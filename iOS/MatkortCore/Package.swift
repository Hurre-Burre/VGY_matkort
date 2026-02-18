// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "MatkortCore",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "MatkortCore", targets: ["MatkortCore"])
    ],
    targets: [
        .target(name: "MatkortCore"),
        .testTarget(name: "MatkortCoreTests", dependencies: ["MatkortCore"])
    ]
)
