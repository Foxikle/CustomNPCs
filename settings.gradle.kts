plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}
rootProject.name = "customnpcs"

include("core")
include("api")
include("v1_20_R3")
include("v1_20_R2")
include("v1_20_R1")
