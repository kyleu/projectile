"use strict";
exports.__esModule = true;
var fs_1 = require("fs");
var ts = require("typescript");
function ast(src, tgt) {
    var sourceFile = ts.createSourceFile(src, fs_1.readFileSync(src).toString(), ts.ScriptTarget.ES2015, true);
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
    var t = tgt === undefined ? src.replace(".ts", ".json") : tgt;
    fs_1.writeFileSync(t, json);
    process.exit(0);
}
ast(process.argv[2], process.argv[3]);
