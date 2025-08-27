///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.lwjgl:lwjgl:3.3.3
//DEPS org.lwjgl:lwjgl-glfw:3.3.3
//DEPS org.lwjgl:lwjgl-opengl:3.3.3

//DEPS org.lwjgl:lwjgl:3.3.3:natives-windows
//DEPS org.lwjgl:lwjgl-glfw:3.3.3:natives-windows
//DEPS org.lwjgl:lwjgl-opengl:3.3.3:natives-windows

//DEPS org.lwjgl:lwjgl:3.3.3:natives-linux
//DEPS org.lwjgl:lwjgl-glfw:3.3.3:natives-linux
//DEPS org.lwjgl:lwjgl-opengl:3.3.3:natives-linux

//DEPS org.lwjgl:lwjgl:3.3.3:natives-macos
//DEPS org.lwjgl:lwjgl-glfw:3.3.3:natives-macos
//DEPS org.lwjgl:lwjgl-opengl:3.3.3:natives-macos

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class IndianFlag {
    
    // Window handle
    private long window;
    
    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        
        init();
        loop();
        
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
    
    private void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();
        
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        // Create the window
        window = glfwCreateWindow(800, 600, "OpenGL Rectangle", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // Setup a key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });
        
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);
            
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            
            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        
        // Enable v-sync
        glfwSwapInterval(1);
        
        // Make the window visible
        glfwShowWindow(window);
    }
    
    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();
        
        // Set the clear color (background color)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black background
        
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Draw the rectangle
            drawRectangle();
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    private void drawRectangle() {
        // Total flag dimensions with 3:2 aspect ratio
        float totalWidth = 0.9f;   // Total width of the flag
        float totalHeight = 0.6f;  // Total height of the flag (0.9 * 2/3 = 0.6)
        float rectHeight = totalHeight / 3.0f; // Height of each individual rectangle
        
        // Center coordinates
        float centerX = 0.0f;
        float centerY = 0.0f;
        
        // Calculate boundaries
        float left = centerX - totalWidth / 2.0f;   // -0.45
        float right = centerX + totalWidth / 2.0f;  // 0.45
        float top = centerY + totalHeight / 2.0f;   // 0.3
        float bottom = centerY - totalHeight / 2.0f; // -0.3
        
        // First rectangle - Saffron color (top)
        glColor3f(1.0f, 0.6f, 0.2f); // Saffron RGB approximation
        glBegin(GL_QUADS);
            glVertex2f(left,  top - rectHeight);    // Bottom-left
            glVertex2f(right, top - rectHeight);    // Bottom-right
            glVertex2f(right, top);                 // Top-right
            glVertex2f(left,  top);                 // Top-left
        glEnd();
        
        // Second rectangle - White color (middle)
        glColor3f(1.0f, 1.0f, 1.0f); // White
        glBegin(GL_QUADS);
            glVertex2f(left,  top - 2 * rectHeight); // Bottom-left
            glVertex2f(right, top - 2 * rectHeight); // Bottom-right
            glVertex2f(right, top - rectHeight);     // Top-right
            glVertex2f(left,  top - rectHeight);     // Top-left
        glEnd();
        
        // Third rectangle - Green color (bottom)
        glColor3f(0.0f, 0.5f, 0.0f); // Green
        glBegin(GL_QUADS);
            glVertex2f(left,  bottom);              // Bottom-left
            glVertex2f(right, bottom);              // Bottom-right
            glVertex2f(right, top - 2 * rectHeight); // Top-right
            glVertex2f(left,  top - 2 * rectHeight); // Top-left
        glEnd();
        
        // Draw the Ashoka Chakra in the center of white stripe
        drawAshokachakra(centerX, centerY, rectHeight * 0.4f);
    }
    
    private void drawAshokachakra(float centerX, float centerY, float radius) {
        int spokes = 24;
        int circleSegments = 60; // For smooth circle
        
        // Set color to navy blue (Ashoka Chakra color)
        glColor3f(0.0f, 0.0f, 0.5f);
        
        // Draw outer circle
        glLineWidth(3.0f);
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = centerX + radius * (float)Math.cos(angle);
            float y = centerY + radius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Draw inner circle (hub)
        float innerRadius = radius * 0.15f;
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = centerX + innerRadius * (float)Math.cos(angle);
            float y = centerY + innerRadius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Fill inner circle
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(centerX, centerY); // Center point
        for (int i = 0; i <= circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = centerX + innerRadius * (float)Math.cos(angle);
            float y = centerY + innerRadius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Draw 24 spokes
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        for (int i = 0; i < spokes; i++) {
            float angle = 2.0f * (float)Math.PI * i / spokes;
            
            // Inner point (from hub edge)
            float innerX = centerX + innerRadius * (float)Math.cos(angle);
            float innerY = centerY + innerRadius * (float)Math.sin(angle);
            
            // Outer point (to outer circle)
            float outerX = centerX + radius * (float)Math.cos(angle);
            float outerY = centerY + radius * (float)Math.sin(angle);
            
            glVertex2f(innerX, innerY);
            glVertex2f(outerX, outerY);
        }
        glEnd();
    }
    
    public static void main(String[] args) {
        new IndianFlag().run();
    }
}

/*
 * JBang Usage:
 * 
 * To run this file:
 * 1. Save this file as IndianFlag.java
 * 2. Run: jbang IndianFlag.java
 * 
 * JBang will automatically download all dependencies including
 * native libraries for your platform (Windows, Linux, macOS).
 * 
 * Make sure you have JBang installed:
 * - Install via SDK: sdk install jbang
 * - Or via Homebrew: brew install jbang
 * - Or download from: https://www.jbang.dev/download/
 */
