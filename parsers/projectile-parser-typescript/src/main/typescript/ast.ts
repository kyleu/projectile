import {readFileSync, writeFileSync} from "fs";
import * as ts from "typescript";

function ast(src: string, tgt: string): void {
  let sourceFile = ts.createSourceFile(src, readFileSync(src).toString(), ts.ScriptTarget.ES2015, true);
  let json = JSON.stringify(sourceFile, (key, value) => {
    switch(key) {
      case 'parent':
        return (value === null || value === undefined) ? null : value.id;
      case 'nextContainer' || 'flowNode' || 'emitNode':
        return null;
      default:
        return value;
    }
  }, 2);
  let t = tgt === undefined ? src.replace(".ts", ".json") : tgt;
  writeFileSync(t, json);
  process.exit(0);
}

ast(process.argv[2], process.argv[3]);
