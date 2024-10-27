package com.aleksa.constants

fun processPrimitive(method: String, args: List<Any?>): Any? {
	val first = args.getOrNull(0)
	val second = args.getOrNull(1)
	return when(method) {
		"times" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.times(second)
				is Short -> first.times(second)
				is Int -> first.times(second)
				is Long -> first.times(second)
				is Float -> first.times(second)
				is Double -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.times(second)
				is UShort -> first.times(second)
				is UInt -> first.times(second)
				is ULong -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.times(second)
				is UShort -> first.times(second)
				is UInt -> first.times(second)
				is ULong -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.times(second)
				is UShort -> first.times(second)
				is UInt -> first.times(second)
				is ULong -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.times(second)
				is UShort -> first.times(second)
				is UInt -> first.times(second)
				is ULong -> first.times(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"div" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.div(second)
				is Short -> first.div(second)
				is Int -> first.div(second)
				is Long -> first.div(second)
				is Float -> first.div(second)
				is Double -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.div(second)
				is UShort -> first.div(second)
				is UInt -> first.div(second)
				is ULong -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.div(second)
				is UShort -> first.div(second)
				is UInt -> first.div(second)
				is ULong -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.div(second)
				is UShort -> first.div(second)
				is UInt -> first.div(second)
				is ULong -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.div(second)
				is UShort -> first.div(second)
				is UInt -> first.div(second)
				is ULong -> first.div(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"plus" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Char -> when(second) {
				is Int -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.plus(second)
				is Short -> first.plus(second)
				is Int -> first.plus(second)
				is Long -> first.plus(second)
				is Float -> first.plus(second)
				is Double -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.plus(second)
				is UShort -> first.plus(second)
				is UInt -> first.plus(second)
				is ULong -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.plus(second)
				is UShort -> first.plus(second)
				is UInt -> first.plus(second)
				is ULong -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.plus(second)
				is UShort -> first.plus(second)
				is UInt -> first.plus(second)
				is ULong -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.plus(second)
				is UShort -> first.plus(second)
				is UInt -> first.plus(second)
				is ULong -> first.plus(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"minus" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Char -> when(second) {
				is Int -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.minus(second)
				is Short -> first.minus(second)
				is Int -> first.minus(second)
				is Long -> first.minus(second)
				is Float -> first.minus(second)
				is Double -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.minus(second)
				is UShort -> first.minus(second)
				is UInt -> first.minus(second)
				is ULong -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.minus(second)
				is UShort -> first.minus(second)
				is UInt -> first.minus(second)
				is ULong -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.minus(second)
				is UShort -> first.minus(second)
				is UInt -> first.minus(second)
				is ULong -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.minus(second)
				is UShort -> first.minus(second)
				is UInt -> first.minus(second)
				is ULong -> first.minus(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"compareTo" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Char -> when(second) {
				is Char -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.compareTo(second)
				is Short -> first.compareTo(second)
				is Int -> first.compareTo(second)
				is Long -> first.compareTo(second)
				is Float -> first.compareTo(second)
				is Double -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.compareTo(second)
				is UShort -> first.compareTo(second)
				is UInt -> first.compareTo(second)
				is ULong -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.compareTo(second)
				is UShort -> first.compareTo(second)
				is UInt -> first.compareTo(second)
				is ULong -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.compareTo(second)
				is UShort -> first.compareTo(second)
				is UInt -> first.compareTo(second)
				is ULong -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.compareTo(second)
				is UShort -> first.compareTo(second)
				is UInt -> first.compareTo(second)
				is ULong -> first.compareTo(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"mod" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.mod(second)
				is Short -> first.mod(second)
				is Int -> first.mod(second)
				is Long -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is Double -> when(second) {
				is Float -> first.mod(second)
				is Double -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is Float -> when(second) {
				is Float -> first.mod(second)
				is Double -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.mod(second)
				is Short -> first.mod(second)
				is Int -> first.mod(second)
				is Long -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.mod(second)
				is Short -> first.mod(second)
				is Int -> first.mod(second)
				is Long -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.mod(second)
				is Short -> first.mod(second)
				is Int -> first.mod(second)
				is Long -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.mod(second)
				is UShort -> first.mod(second)
				is UInt -> first.mod(second)
				is ULong -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.mod(second)
				is UShort -> first.mod(second)
				is UInt -> first.mod(second)
				is ULong -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.mod(second)
				is UShort -> first.mod(second)
				is UInt -> first.mod(second)
				is ULong -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.mod(second)
				is UShort -> first.mod(second)
				is UInt -> first.mod(second)
				is ULong -> first.mod(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"floorDiv" -> when(first) {
			is Byte -> when(second) {
				is Byte -> first.floorDiv(second)
				is Short -> first.floorDiv(second)
				is Int -> first.floorDiv(second)
				is Long -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is Int -> when(second) {
				is Byte -> first.floorDiv(second)
				is Short -> first.floorDiv(second)
				is Int -> first.floorDiv(second)
				is Long -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is Long -> when(second) {
				is Byte -> first.floorDiv(second)
				is Short -> first.floorDiv(second)
				is Int -> first.floorDiv(second)
				is Long -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is Short -> when(second) {
				is Byte -> first.floorDiv(second)
				is Short -> first.floorDiv(second)
				is Int -> first.floorDiv(second)
				is Long -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is UByte -> when(second) {
				is UByte -> first.floorDiv(second)
				is UShort -> first.floorDiv(second)
				is UInt -> first.floorDiv(second)
				is ULong -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is UInt -> when(second) {
				is UByte -> first.floorDiv(second)
				is UShort -> first.floorDiv(second)
				is UInt -> first.floorDiv(second)
				is ULong -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is ULong -> when(second) {
				is UByte -> first.floorDiv(second)
				is UShort -> first.floorDiv(second)
				is UInt -> first.floorDiv(second)
				is ULong -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			is UShort -> when(second) {
				is UByte -> first.floorDiv(second)
				is UShort -> first.floorDiv(second)
				is UInt -> first.floorDiv(second)
				is ULong -> first.floorDiv(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"unaryPlus" -> when(first) {
			is Byte -> first.unaryPlus()
			is Double -> first.unaryPlus()
			is Float -> first.unaryPlus()
			is Int -> first.unaryPlus()
			is Long -> first.unaryPlus()
			is Short -> first.unaryPlus()
			else -> Evaluator.UnknownValue
		}
		"unaryMinus" -> when(first) {
			is Byte -> first.unaryMinus()
			is Double -> first.unaryMinus()
			is Float -> first.unaryMinus()
			is Int -> first.unaryMinus()
			is Long -> first.unaryMinus()
			is Short -> first.unaryMinus()
			else -> Evaluator.UnknownValue
		}
		"toInt" -> when(first) {
			is Byte -> first.toInt()
			is Double -> first.toInt()
			is Float -> first.toInt()
			is Int -> first.toInt()
			is Long -> first.toInt()
			is Short -> first.toInt()
			else -> Evaluator.UnknownValue
		}
		"toLong" -> when(first) {
			is Byte -> first.toLong()
			is Double -> first.toLong()
			is Float -> first.toLong()
			is Int -> first.toLong()
			is Long -> first.toLong()
			is Short -> first.toLong()
			else -> Evaluator.UnknownValue
		}
		"toFloat" -> when(first) {
			is Byte -> first.toFloat()
			is Double -> first.toFloat()
			is Float -> first.toFloat()
			is Int -> first.toFloat()
			is Long -> first.toFloat()
			is Short -> first.toFloat()
			else -> Evaluator.UnknownValue
		}
		"toDouble" -> when(first) {
			is Byte -> first.toDouble()
			is Double -> first.toDouble()
			is Float -> first.toDouble()
			is Int -> first.toDouble()
			is Long -> first.toDouble()
			is Short -> first.toDouble()
			else -> Evaluator.UnknownValue
		}
		"inc" -> when(first) {
			is Byte -> first.inc()
			is Double -> first.inc()
			is Float -> first.inc()
			is Int -> first.inc()
			is Long -> first.inc()
			is Short -> first.inc()
			is UByte -> first.inc()
			is UInt -> first.inc()
			is ULong -> first.inc()
			is UShort -> first.inc()
			else -> Evaluator.UnknownValue
		}
		"dec" -> when(first) {
			is Byte -> first.dec()
			is Double -> first.dec()
			is Float -> first.dec()
			is Int -> first.dec()
			is Long -> first.dec()
			is Short -> first.dec()
			is UByte -> first.dec()
			is UInt -> first.dec()
			is ULong -> first.dec()
			is UShort -> first.dec()
			else -> Evaluator.UnknownValue
		}
		"toByte" -> when(first) {
			is Byte -> first.toByte()
			is Int -> first.toByte()
			is Long -> first.toByte()
			is Short -> first.toByte()
			else -> Evaluator.UnknownValue
		}
		"toShort" -> when(first) {
			is Byte -> first.toShort()
			is Int -> first.toShort()
			is Long -> first.toShort()
			is Short -> first.toShort()
			else -> Evaluator.UnknownValue
		}
		"not" -> when(first) {
			is Boolean -> first.not()
			else -> Evaluator.UnknownValue
		}
		"or" -> when(first) {
			is Boolean -> when(second) {
				is Boolean -> first.or(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"and" -> when(first) {
			is Boolean -> when(second) {
				is Boolean -> first.and(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		"xor" -> when(first) {
			is Boolean -> when(second) {
				is Boolean -> first.xor(second)
				else -> Evaluator.UnknownValue
			}
			else -> Evaluator.UnknownValue
		}
		else -> Evaluator.UnknownValue
	}
}

fun processComparison(method: String, args: List<Any?>): Any? {
	val first = args.getOrNull(0)
	val second = args.getOrNull(1)
	return when(method) {
		"EQEQ" -> when(first) {
			is Int? -> if (second is Int?) first == second else Evaluator.UnknownValue
			is Long? -> if (second is Long?) first == second else Evaluator.UnknownValue
			is Float? -> if (second is Float?) first == second else Evaluator.UnknownValue
			is Double? -> if (second is Double?) first == second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		"ieee754equals" -> when(first) {
			is Int? -> if (second is Int?) first == second else Evaluator.UnknownValue
			is Long? -> if (second is Long?) first == second else Evaluator.UnknownValue
			is Float? -> if (second is Float?) first == second else Evaluator.UnknownValue
			is Double? -> if (second is Double?) first == second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		"less" -> when(first) {
			is Int -> if (second is Int) first < second else Evaluator.UnknownValue
			is Long -> if (second is Long) first < second else Evaluator.UnknownValue
			is Float -> if (second is Float) first < second else Evaluator.UnknownValue
			is Double -> if (second is Double) first < second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		"greater" -> when(first) {
			is Int -> if (second is Int) first > second else Evaluator.UnknownValue
			is Long -> if (second is Long) first > second else Evaluator.UnknownValue
			is Float -> if (second is Float) first > second else Evaluator.UnknownValue
			is Double -> if (second is Double) first > second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		"lessOrEqual" -> when(first) {
			is Int -> if (second is Int) first <= second else Evaluator.UnknownValue
			is Long -> if (second is Long) first <= second else Evaluator.UnknownValue
			is Float -> if (second is Float) first <= second else Evaluator.UnknownValue
			is Double -> if (second is Double) first <= second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		"greaterOrEqual" -> when(first) {
			is Int -> if (second is Int) first >= second else Evaluator.UnknownValue
			is Long -> if (second is Long) first >= second else Evaluator.UnknownValue
			is Float -> if (second is Float) first >= second else Evaluator.UnknownValue
			is Double -> if (second is Double) first >= second else Evaluator.UnknownValue
			else -> Evaluator.UnknownValue
		}
		else -> Evaluator.UnknownValue
	}
}
