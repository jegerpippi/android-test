/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.web.util.concurrent;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import kotlin.jvm.functions.Function1;

/**
 * Forked from androidx.camera package to avoid full Guava dependency.
 *
 * <p>Utility class for generating specific implementations of {@link ListenableFuture}.
 */
public final class Futures {

  /**
   * Returns an implementation of {@link ListenableFuture} which immediately contains a result.
   *
   * @param value The result that is immediately set on the future.
   * @param <V> The type of the result.
   * @return A future which immediately contains the result.
   */
  @NonNull
  public static <V> ListenableFuture<V> immediateFuture(@Nullable V value) {
    if (value == null) {
      return ImmediateFuture.nullFuture();
    }

    return new ImmediateFuture.ImmediateSuccessfulFuture<>(value);
  }

  /**
   * Returns an implementation of {@link ListenableFuture} which immediately contains an exception
   * that will be thrown by {@link Future#get()}.
   *
   * @param cause The cause of the {@link ExecutionException} that will be thrown by {@link
   *     Future#get()}.
   * @param <V> The type of the result.
   * @return A future which immediately contains an exception.
   */
  @NonNull
  public static <V> ListenableFuture<V> immediateFailedFuture(@NonNull Throwable cause) {
    return new ImmediateFuture.ImmediateFailedFuture<>(cause);
  }

  /**
   * Returns a new {@code Future} whose result is asynchronously derived from the result of the
   * given {@code Future}. If the given {@code Future} fails, the returned {@code Future} fails with
   * the same exception (and the function is not invoked).
   *
   * @param input The future to transform
   * @param function A function to transform the result of the input future to the result of the
   *     output future
   * @param executor Executor to run the function in.
   * @return A future that holds result of the function (if the input succeeded) or the original
   *     input's failure (if not)
   */
  @NonNull
  public static <I, O> ListenableFuture<O> transformAsync(
      @NonNull ListenableFuture<I> input,
      @NonNull AsyncFunction<? super I, ? extends O> function,
      @NonNull Executor executor) {
    ChainingListenableFuture<I, O> output = new ChainingListenableFuture<I, O>(function, input);
    input.addListener(output, executor);
    return output;
  }

  /**
   * Returns a new {@code Future} whose result is derived from the result of the given {@code
   * Future}. If {@code input} fails, the returned {@code Future} fails with the same exception (and
   * the function is not invoked)
   *
   * @param input The future to transform
   * @param function A function to transform the results of the provided future to the results of
   *     the returned future.
   * @param executor Executor to run the function in.
   * @return A future that holds result of the transformation.
   */
  @NonNull
  public static <I, O> ListenableFuture<O> transform(
      @NonNull ListenableFuture<I> input,
      @NonNull Function1<? super I, ? extends O> function,
      @NonNull Executor executor) {
    checkNotNull(function);
    return transformAsync(
        input,
        new AsyncFunction<I, O>() {

          @NonNull
          @Override
          public ListenableFuture<O> apply(I input) {
            return immediateFuture(function.invoke(input));
          }
        },
        executor);
  }

  /**
   * Registers separate success and failure callbacks to be run when the {@code Future}'s
   * computation is {@linkplain java.util.concurrent.Future#isDone() complete} or, if the
   * computation is already complete, immediately.
   *
   * @param future The future attach the callback to.
   * @param callback The callback to invoke when {@code future} is completed.
   * @param executor The executor to run {@code callback} when the future completes.
   */
  public static <V> void addCallback(
      @NonNull final ListenableFuture<V> future,
      @NonNull final FutureCallback<? super V> callback,
      @NonNull Executor executor) {
    checkNotNull(callback);
    future.addListener(new CallbackListener<V>(future, callback), executor);
  }

  /** See {@link #addCallback(ListenableFuture, FutureCallback, Executor)} for behavioral notes. */
  private static final class CallbackListener<V> implements Runnable {
    final Future<V> mFuture;
    final FutureCallback<? super V> mCallback;

    CallbackListener(Future<V> future, FutureCallback<? super V> callback) {
      mFuture = future;
      mCallback = callback;
    }

    @Override
    public void run() {
      final V value;
      try {
        value = getDone(mFuture);
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
          mCallback.onFailure(e);
        } else {
          mCallback.onFailure(cause);
        }
        return;
      } catch (RuntimeException | Error e) {
        mCallback.onFailure(e);
        return;
      }
      mCallback.onSuccess(value);
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "," + mCallback;
    }
  }

  /**
   * Returns the result of the input {@code Future}, which must have already completed.
   *
   * <p>The benefits of this method are twofold. First, the name "getDone" suggests to readers that
   * the {@code Future} is already done. Second, if buggy code calls {@code getDone} on a {@code
   * Future} that is still pending, the program will throw instead of block.
   *
   * @throws ExecutionException if the {@code Future} failed with an exception
   * @throws CancellationException if the {@code Future} was cancelled
   * @throws IllegalStateException if the {@code Future} is not done
   */
  @Nullable
  public static <V> V getDone(@NonNull Future<V> future) throws ExecutionException {
    /*
     * We throw IllegalStateException, since the call could succeed later. Perhaps we
     * "should" throw IllegalArgumentException, since the call could succeed with a different
     * argument. Those exceptions' docs suggest that either is acceptable. Google's Java
     * Practices page recommends IllegalArgumentException here, in part to keep its
     * recommendation simple: Static methods should throw IllegalStateException only when
     * they use static state.
     *
     * Why do we deviate here? The answer: We want for fluentFuture.getDone() to throw the same
     * exception as Futures.getDone(fluentFuture).
     */
    checkState(future.isDone(), "Future was expected to be done, " + future);
    return getUninterruptibly(future);
  }

  /**
   * Invokes {@code Future.}{@link Future#get() get()} uninterruptibly.
   *
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   */
  @Nullable
  public static <V> V getUninterruptibly(@NonNull Future<V> future) throws ExecutionException {
    boolean interrupted = false;
    try {
      while (true) {
        try {
          return future.get();
        } catch (InterruptedException e) {
          interrupted = true;
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Should not be instantiated. */
  private Futures() {}
}
