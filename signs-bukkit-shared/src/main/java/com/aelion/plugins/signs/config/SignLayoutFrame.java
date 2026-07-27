package com.aelion.plugins.signs.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;

public final class SignLayoutFrame {

    private final List<String> lines;
    private final Material blockMaterial;

    public SignLayoutFrame(List<String> lines, Material blockMaterial) {
        List<String> padded = new ArrayList<String>(4);
        for (int i = 0; i < 4; i++) {
            padded.add(i < lines.size() && lines.get(i) != null ? lines.get(i) : "");
        }
        this.lines = Collections.unmodifiableList(padded);
        this.blockMaterial = blockMaterial == null ? Material.AIR : blockMaterial;
    }

    public List<String> lines() {
        return lines;
    }

    public Material blockMaterial() {
        return blockMaterial;
    }
}
