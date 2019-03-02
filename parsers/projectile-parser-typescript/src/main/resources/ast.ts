import {readFileSync, writeFileSync} from "fs";
import * as ts from "typescript";

function ast(filename: string): void {
  let sourceFile = ts.createSourceFile(filename, readFileSync(filename).toString(), ts.ScriptTarget.ES2015, true);
  switch (sourceFile.kind) {
    case ts.SyntaxKind.SourceFile:
      console.info(ts.SyntaxKind.SourceFile.toString());
      break
  }
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
  writeFileSync(filename.replace(".ts", ".json"), json);
  process.exit(0);
}

ast(process.argv[2]);

