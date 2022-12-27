/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.minecraft.client.model.*

class TexturedModel {
    @SerializedName("format_version")
    val formatVersion: String = "0"
    @SerializedName("minecraft:geometry")
    val geometry: List<ModelGeometry>? = null

    fun create() : TexturedModelData {
        val modelData = ModelData()
        val parts = HashMap<String, ModelPartData>()
        val bones = HashMap<String, ModelBone>()

        try {
            val geometry = this.geometry!![0]
            val geometryBones = geometry.bones!! 
            var parentPart: ModelPartData

            for (bone in geometryBones) {
                bones[bone.name] = bone
                parentPart = if (bone.parent != null) parts[bone.parent]!! else modelData.root

                val modelTransform : ModelTransform
                when {
                    bone.parent == null -> {
                        // The root part always has a 24 Y offset. One of life's great mysteries.
                        modelTransform = ModelTransform.pivot(0F, 24F, 0F)
                    }
                    bone.rotation != null -> {
                        modelTransform = ModelTransform.of(
                            -(bones[bone.parent]!!.pivot[0] - bone.pivot[0]),
                            bones[bone.parent]!!.pivot[1] - bone.pivot[1],
                            -(bones[bone.parent]!!.pivot[2] - bone.pivot[2]),
                            Math.toRadians(bone.rotation[0].toDouble()).toFloat(),
                            Math.toRadians(bone.rotation[1].toDouble()).toFloat(),
                            Math.toRadians(bone.rotation[2].toDouble()).toFloat()
                        )
                    }
                    else -> {
                        modelTransform = ModelTransform.pivot(
                            -(bones[bone.parent]!!.pivot[0] - bone.pivot[0]),
                            bones[bone.parent]!!.pivot[1] - bone.pivot[1],
                            -(bones[bone.parent]!!.pivot[2] - bone.pivot[2])
                        )
                    }
                }

                val modelPart = ModelPartBuilder.create()
                val subParts = mutableListOf<ModelPartBuilder>()
                val modelTransforms = mutableListOf<ModelTransform>()

                if (bone.cubes != null) {
                    var pivot: List<Float>
                    var subPart: ModelPartBuilder

                    for (cube in bone.cubes) {
                        subPart = if (cube.rotation != null) ModelPartBuilder.create() else modelPart
                        pivot = cube.pivot ?: bone.pivot

                        if (cube.uv != null) {
                            subPart.uv(
                                cube.uv[0],
                                cube.uv[1]
                            )
                        }
                        if (cube.mirror != null && cube.mirror == true) {
                            subPart.mirrored()
                        }
                        if (cube.size != null && cube.origin != null) {
                            subPart.cuboid(
                                cube.origin[0] - pivot[0],
                                // Y is inverted in Java Edition, but that also means counting from the other side of the cube.
                                -(cube.origin[1] - pivot[1] + cube.size[1]),
                                cube.origin[2] - pivot[2],
                                cube.size[0],
                                cube.size[1],
                                cube.size[2],
                                Dilation(cube.inflate ?: 0f)
                            )
                        }
                        if (cube.mirror != null && cube.mirror == true) {
                            subPart.mirrored(false)
                        }

                        if (subPart != modelPart) {
                            modelTransforms.add(ModelTransform.of(
                                -(bone.pivot[0] - cube.pivot!![0]),
                                bone.pivot[1] - cube.pivot[1],
                                -(bone.pivot[2] - cube.pivot[2]),
                                Math.toRadians(cube.rotation!![0].toDouble()).toFloat(),
                                Math.toRadians(cube.rotation[1].toDouble()).toFloat(),
                                Math.toRadians(cube.rotation[2].toDouble()).toFloat()
                            ))
                            subParts.add(subPart)
                        }
                    }
                }

                parts.put(
                    bone.name,
                    parentPart.addChild(
                        bone.name,
                        modelPart,
                        modelTransform
                    )
                )

                var counter = 0
                subParts.forEachIndexed { index, part ->
                    parts[bone.name]!!.addChild(
                        bone.name + counter++.toString(),
                        part,
                        modelTransforms[index]
                    )
                }
            }

            return TexturedModelData.of(modelData, geometry.description.textureWidth, geometry.description.textureHeight)
        } catch (e: Exception) {
            if (geometry != null) {
                throw IllegalArgumentException("Error creating TexturedModelData with identifier ${geometry[0].description.identifier}.", e)
            } else {
                throw IllegalArgumentException("Error creating TexturedModelData.", e)
            }
        }
    }

    companion object {
        val GSON = GsonBuilder()
            .setLenient()
            .create()

        fun from(json: String) : TexturedModel {
            try {
                return GSON.fromJson(json, TexturedModel::class.java)
            } catch (exception: Exception) {
                throw IllegalStateException("Issue loading pokemon geo: $json", exception)
            }
        }
    }
}
class ModelGeometry {
    lateinit var description: ModelDataDescription
    val bones: List<ModelBone>? = null
}
class ModelDataDescription(
    val identifier: String,
    @SerializedName("texture_width")
    val textureWidth: Int,
    @SerializedName("texture_height")
    val textureHeight: Int,
    @SerializedName("visible_bounds_width")
    val visibleBoundsWidth: Float,
    @SerializedName("visible_bounds_height")
    val visibleBoundsHeight: Float,
    @SerializedName("visible_bounds_offset")
    val visibleBoundsOffset: List<Float>
) { }
class ModelBone {
    val name: String = ""
    val parent: String? = null
    val pivot: List<Float> = emptyList()
    val rotation: List<Float>? = null
    val cubes: List<Cube>? = null
}
class Cube {
    val origin: List<Float>? = null
    val size: List<Float>? = null
    val pivot: List<Float>? = null
    val rotation: List<Float>? = null
    val uv: List<Int>? = null
    val inflate: Float? = null
    val mirror: Boolean = false
}