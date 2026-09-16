const std = @import("std");

export fn gypsum_main(argc: c_int, argv: [*][*]u8) c_int {
    _ = argc;
    _ = argv;

    const stdout = std.io.getStdOut().writer();
    stdout.print("Hello from Zig on Gypsum!\n", .{}) catch return 1;
    stdout.print("Loaded via dlopen on Android.\n", .{}) catch return 1;
    return 0;
}
