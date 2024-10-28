## Kotlin constant evaluation task
This project is based on the template from the recommended [article](https://blog.bnorm.dev/writing-your-second-compiler-plugin-part-1).
The **ir-plugin** project contains the entirety of my implementation as well as unit tests.

The **compile-test** project is configured to be compiled with the plugin, and can be used to test out the plugin manually. You can decompile the compiled MainKt class into java to see what changes were made, or uncomment `say(it.dump())` in [KotlinIrGeneratorExtension](.ir-plugin/src/main/kotlin/com/aleksa/constants/KotlinIrGenerationExtension.kt) to see the dump of the resulting IR tree printed to the terminal.

## Implementation
The plugin essentially consists of 3 classes: Context, Evaluator and Replacer.

### Context
[This class](./ir-plugin/src/main/kotlin/com/aleksa/constants/Context.kt) represents a stack frame and is used during evaluation to keep track of known variables. Unknown values are represented as `Evaluator.Unknown`.

It never accepts writes to mutable global variables and local variables captured and modified in closures, since these are generally volatile. It can be configured to ignore such attempts (`mutable = true`) or throw an exception (`mutabe = false`). The latter is used when a code fragment is evaluated with the intent of replacing it, in which case side effects would make that replacement impossible.

### Evaluator
[This class](./ir-plugin/src/main/kotlin/com/aleksa/constants/Evaluator.kt) interprets code within a given context. It can handle unknown variables. Each method returns the result of the interpreted code fragment, and this might be:
1. `Unknown`: The result could not be resolved with the given Context information. Subclasses of this are:
    - `UnknownValue`: No flow change.
    - `PossibleFlowChange`: The result might be a value, but it might also be a flow change (a return, break or continue statement).
2. `FlowChange`: The result of executing this code fragment is a flow change. Subclasses are `Return`, `Break` and `Continue`.
3. Other: Anything else means that a result value was successfully obtained.

Besides all the expression types listed in the task, this class can also interpret lambda expressions and lambda invocations. Also, by default it will attempt to interpet all method invocations, but this can be changed by passing a predicate to the constructor. In my test cases a didn't prefix most methods with eval, but they are all evaluated.

You might notice a [python file](./ir-plugin/src/main/kotlin/com/aleksa/constants/process_primitives_generator.py) in the source directory. I used this to create the [processPrimitive.kt](./ir-plugin/src/main/kotlin/com/aleksa/constants/processPrimitive.kt) file for primitive operations. Not sure if this is a great solution. Relection does not work for primitive operators.

### Replacer
[This class](./ir-plugin/src/main/kotlin/com/aleksa/constants/Replacer.kt) visits all declarations and expressions in a file, and simplifies expressions by utilizing the evaluator.

## Testing
I wrote unit tests covering [helper methods](./ir-plugin/src/test/kotlin/com/aleksa/constants/BasicTests.kt), [evaluation](./ir-plugin/src/test/kotlin/com/aleksa/constants/EvaluatorTests.kt) and [replacement](./ir-plugin/src/test/kotlin/com/aleksa/constants/ReplacerTests.kt).