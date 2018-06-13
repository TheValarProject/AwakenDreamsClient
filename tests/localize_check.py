#!/usr/bin/env python3
import os.path
import sys

names = set()
finished_files = []

def check_file(path):
    filename = os.path.basename(path)
    if filename.startswith('.'):
        return
    curNames = set()
    with open(path, 'r') as f:
        for line in f:
            if not "=" in line:
                continue
            unlocalizedName = line[0:line.index('=')]
            if not line[line.index('=')+1:].strip():
                print("Warning: Empty value for key '" + unlocalizedName + "' in '" + filename + "'")
            if unlocalizedName in curNames:
                print("Warning: Duplicate key '" + unlocalizedName + "' in '" + filename + "'", file=sys.stderr)
            else:
                curNames.add(unlocalizedName)
            if not unlocalizedName in names:
                for finished_file in finished_files:
                    print("Warning: Missing key '" + unlocalizedName + "' in '" + finished_file + "'", file=sys.stderr)
            names.add(unlocalizedName)
        for x in names:
            if not x in curNames:
                print("Warning: Missing key '" + x + "' in '" + filename + "'", file=sys.stderr)
    finished_files.append(filename)

if __name__ == "__main__":
    langDir = os.path.join(os.path.dirname(os.path.dirname(os.path.realpath(__file__))), "mcp", "src", "minecraft", "assets", "awakendreams", "lang")
    paths = os.listdir(langDir)
    for path in paths:
        check_file(os.path.join(langDir, path))
