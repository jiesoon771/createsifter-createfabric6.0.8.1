/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.meshes;

public class StringMesh extends BaseMesh {
	public StringMesh(Properties pProperties) {
		super(pProperties);

		this.mesh = MeshTypes.STRING;
	}
}
