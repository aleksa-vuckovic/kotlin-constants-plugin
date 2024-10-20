package com.aleksa.constants

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class KotlinGradleExtension(objects: ObjectFactory) {
    val stringProperty: Property<String> = objects.property(String::class.java)
    val fileProperty: RegularFileProperty = objects.fileProperty()
}