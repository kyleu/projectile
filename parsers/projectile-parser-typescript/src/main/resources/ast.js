"use strict";
exports.__esModule = true;
var fs_1 = require("fs");
var ts = require("typescript");
function ast(filename) {
    var sourceFile = ts.createSourceFile(filename, fs_1.readFileSync(filename).toString(), ts.ScriptTarget.ES2015, true);
    switch (sourceFile.kind) {
        case ts.SyntaxKind.SourceFile:
            console.info(ts.SyntaxKind.SourceFile.toString());
            break;
    }
    var json = JSON.stringify(sourceFile, function (key, value) {
        switch (key) {
            case 'parent':
                return (value === null || value === undefined) ? null : value.id;
            case 'nextContainer' || 'flowNode' || 'emitNode':
                return null;
            default:
                return value;
        }
    }, 2);
    fs_1.writeFileSync(filename.replace(".ts", ".json"), json);
    process.exit(0);
}
ast(process.argv[2]);
