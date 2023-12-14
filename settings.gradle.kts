plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}
rootProject.name = "customnpcs"

include("core")
include("api")
include("v1_20_R3")
include("v1_20_R2")
include("v1_20_R2:v1_20_R0")
findProject(":v1_20_R2:v1_20_R0")?.name = "v1_20_R0"
include("v1_20_R1")
