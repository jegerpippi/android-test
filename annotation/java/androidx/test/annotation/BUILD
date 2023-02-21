# Description: Build rules for building androidx.test.annotation from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_EXPERIMENTAL_VERSION", "ANDROIDX_ANNOTATION_VERSION", "ANNOTATION_VERSION")
load("//build_extensions:api_checks.bzl", "api_checks")

# all users should reference the equivalent targets in //annotation
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "annotation",
    srcs = glob(
        ["**/*.java"],
    ),
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//annotation"],
    deps = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_annotation_annotation_experimental",
    ],
)

# Generate rules for the release artifacts. This generates three targets
# genrule output: annotation_release_no_deps.jar and annotation_release.aar
# a java_library target libannotation_release_no_deps
axt_release_lib(
    name = "annotation_release",
    # keep all androidx.test.core classes except androidx.test.core.R, since that will be
    # auto-generated by consuming build system
    keep_spec = "androidx/test/annotation/.*",
    remove_spec = "androidx/test/annotation/R[$$\\.]",
    deps = [
        ":annotation",
    ],
)

# annotation currently doesn't have any public API
#dackka_test(
#    name = "annotation_doc",
#    jar = ":annotation_release_all_deploy.jar",
#    src_jar = ":libannotation-src.jar",
#)

maven_artifact(
    name = "annotation_maven_artifact",
    src = ":annotation_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.annotation:annotation-experimental:%s" % ANDROIDX_ANNOTATION_EXPERIMENTAL_VERSION,
    ],
    artifact_id = "annotation",
    group_id = "androidx.test",
    last_updated = "20210901000000",
    src_jar = ":libannotation-src.jar",
    version = "%s" % ANNOTATION_VERSION,
)

api_checks(
    name = "annotation_api",
    runtime_dep = ":annotation",
    src_jar = ":libannotation-src.jar",
)