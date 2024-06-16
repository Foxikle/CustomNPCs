plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}
rootProject.name = "customnpcs"

include("core")
include("api")
include("v1_20_R3")
include("v1_20_R2")
include("v1_20_R1")
include("v1_20_R4")
include("v1_21_R0")

