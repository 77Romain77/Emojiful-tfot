package com.hrznstudio.emojiful;

import org.lwjgl.glfw.GLFW;


public class CommonClass {

    public static boolean shouldKeyBeIgnored(int keyCode){
        return keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT;
    }
}
