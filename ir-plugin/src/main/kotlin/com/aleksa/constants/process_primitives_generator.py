
ints = ["Byte", "Short", "Int", "Long"]
floats = ["Float", "Double"]
uints = ["U" + it for it in ints]
UNKNOWN = "Evaluator.UnknownValue"

specs = [
    {
        "for": ["times", "div"],
        "between": [
            [*ints, *floats],
            [*uints]
        ]
    },
    {
        "for": ["plus", "minus"],
        "between": [
            [*ints, *floats],
            [*uints],
            (["Char"], ["Int"])
        ]
    },
    {
        "for": ["compareTo"],
        "between": [
            [*ints, *floats],
            [*uints],
            ["Char"]
        ]
    },
    {
        "for": ["mod"],
        "between": [
            [*ints],
            [*floats],
            [*uints]
        ]
    },
    {
        "for": ["floorDiv"],
        "between": [
            [*ints],
            [*uints]
        ]
    },
    {
        "for": ["unaryPlus", "unaryMinus", "toInt", "toLong", "toFloat", "toDouble"],
        "single": [*ints, *floats]
    },
    {
        "for": ["inc", "dec"],
        "single": [*ints, *floats, *uints]
    },
    {
        "for": ["toByte", "toShort"],
        "single": [*ints]
    },
    {
        "for": ["not"],
        "single": ["Boolean"]
    },
    {
        "for": ["or", "and", "xor"],
        "between": [
            ["Boolean"]
        ]
    }
]

map = {}
for spec in specs:
    if "between" in spec:
        result = {}
        for typespec in spec["between"]:
            if isinstance(typespec, list):
                for type in typespec:
                    result[type] = result[type] + typespec if type in result else typespec
            else:
                for type in typespec[0]:
                    result[type] = result[type] + typespec[1] if type in result else typespec[1]
    elif "single" in spec:
        result = spec["single"]
    for type in spec["for"]:
        map[type] = result


tabs = 0
file = open("processPrimitive.kt", "w")
newline = True
def text(data: str):
    global newline
    if newline:
        file.write(tabs * "\t")
        newline = False
    file.write(data)
def line(data: str = ""):
    global newline
    text(data)
    file.write("\n")
    newline = True

line("package com.aleksa.constants")
line()
line("fun processPrimitive(method: String, args: List<Any?>): Any? {")
tabs += 1
line("val first = args.getOrNull(0)")
line("val second = args.getOrNull(1)")
line("return when(method) {")
tabs += 1
for op in map:
    line(f"\"{op}\" -> when(first) {{")
    tabs += 1
    for first in sorted(map[op]):
        text(f"is {first} -> ")
        if isinstance(map[op], dict):
            line(f"when(second) {{")
            tabs += 1
            for second in map[op][first]:
                line(f"is {second} -> first.{op}(second)")
            line(f"else -> {UNKNOWN}")
            tabs -= 1
            line("}")
        else:
            line(f"first.{op}()")
    line(f"else -> {UNKNOWN}")
    tabs -= 1
    line("}")
line(f"else -> {UNKNOWN}")
tabs -= 1
line("}")
tabs -= 1
line("}")
line()


comps = ["EQEQ", "ieee754equals", "less", "greater", "lessOrEqual", "greaterOrEqual"]
symbols = ["==", "==", "<", ">", "<=", ">="] 
types = ["Int", "Long", "Float", "Double"]
line("fun processComparison(method: String, args: List<Any?>): Any? {")
tabs += 1
line("val first = args.getOrNull(0)")
line("val second = args.getOrNull(1)")
line("return when(method) {")
tabs += 1
for comp, symbol in zip(comps, symbols):
    line(f"\"{comp}\" -> when(first) {{")
    tabs += 1
    null = "?" if (symbol == "==") else ""
    for type in types:
        line(f"is {type}{null} -> if (second is {type}{null}) first {symbol} second else {UNKNOWN}")
    line(f"else -> {UNKNOWN}")
    tabs -= 1
    line("}")
line(f"else -> {UNKNOWN}")
tabs -= 1
line("}")
tabs -= 1
line("}")
file.close()