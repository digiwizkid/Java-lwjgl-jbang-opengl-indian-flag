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

public class AnimatedIndianFlag {
    
    // Window handle
    private long window;
    
    // Animation variables
    private double startTime;
    private float waveAmplitude = 0.05f;
    private float waveFrequency = 2.0f;
    private float chakraRotation = 0.0f;
    private float chakraSpeed = 45.0f; // degrees per second
    private float flagOpacity = 0.0f;
    private boolean fadeIn = true;
    private float fadeSpeed = 1.0f;
    
    public void run() {
        System.out.println("Animated Indian Flag - LWJGL " + Version.getVersion() + "!");
        
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
        window = glfwCreateWindow(900, 700, "Animated Indian Flag", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // Setup a key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            // Press 'R' to reset animation
            if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
                resetAnimation();
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
        
        // Record start time
        startTime = glfwGetTime();
    }
    
    private void resetAnimation() {
        startTime = glfwGetTime();
        chakraRotation = 0.0f;
        flagOpacity = 0.0f;
        fadeIn = true;
    }
    
    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();
        
        // Enable blending for transparency effects
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Set the clear color (gradient background)
        glClearColor(0.05f, 0.05f, 0.15f, 1.0f); // Dark blue background
        
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double deltaTime = currentTime - startTime;
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Update animations
            updateAnimations((float)deltaTime);
            
            // Draw animated background
            drawAnimatedBackground((float)deltaTime);
            
            // Draw the animated flag
            drawAnimatedFlag((float)deltaTime);
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    private void updateAnimations(float time) {
        // Update chakra rotation
        chakraRotation = (chakraSpeed * time) % 360.0f;
        
        // Update flag opacity with fade in/out effect
        if (fadeIn) {
            flagOpacity += fadeSpeed * (1.0f / 60.0f); // Assuming 60 FPS
            if (flagOpacity >= 1.0f) {
                flagOpacity = 1.0f;
                fadeIn = false;
            }
        }
    }
    
    private void drawAnimatedBackground(float time) {
        // Draw subtle animated gradient background
        glBegin(GL_QUADS);
            // Top gradient (lighter)
            glColor4f(0.1f + 0.05f * (float)Math.sin(time * 0.5f), 
                     0.1f + 0.05f * (float)Math.cos(time * 0.3f), 
                     0.2f + 0.05f * (float)Math.sin(time * 0.4f), 1.0f);
            glVertex2f(-1.0f, 1.0f);  // Top-left
            glVertex2f(1.0f, 1.0f);   // Top-right
            
            // Bottom gradient (darker)
            glColor4f(0.02f, 0.02f, 0.1f, 1.0f);
            glVertex2f(1.0f, -1.0f);  // Bottom-right
            glVertex2f(-1.0f, -1.0f); // Bottom-left
        glEnd();
    }
    
    private void drawAnimatedFlag(float time) {
        // Total flag dimensions with 3:2 aspect ratio
        float totalWidth = 0.9f;
        float totalHeight = 0.6f;
        float rectHeight = totalHeight / 3.0f;
        
        // Center coordinates
        float centerX = 0.0f;
        float centerY = 0.0f;
        
        // Calculate boundaries
        float left = centerX - totalWidth / 2.0f;
        float right = centerX + totalWidth / 2.0f;
        float top = centerY + totalHeight / 2.0f;
        float bottom = centerY - totalHeight / 2.0f;
        
        // Wave effect parameters
        int segments = 40; // Even more segments for ultra-smooth wave
        float segmentWidth = totalWidth / segments;
        
        // Pre-calculate all stripe boundary positions to ensure perfect alignment
        float[][] stripeBoundaries = new float[4][segments + 1]; // 4 horizontal boundaries (top, middle1, middle2, bottom)
        
        for (int i = 0; i <= segments; i++) {
            float x = left + i * segmentWidth;
            
            // Calculate wave effects
            float mainWave = waveAmplitude * (float)Math.sin(waveFrequency * (time + x * 2.0f));
            float secondaryWave = waveAmplitude * 0.7f * (float)Math.sin(waveFrequency * (time + x * 2.2f + 0.3f));
            float microWave = 0.005f * (float)Math.sin(time * 4.0f + x * 8.0f);
            
            // Top boundary (fixed to flag top with slight wave for natural look)
            stripeBoundaries[0][i] = top + mainWave * 0.3f + microWave;
            
            // First middle boundary (between saffron and white)
            stripeBoundaries[1][i] = top - rectHeight + mainWave + microWave;
            
            // Second middle boundary (between white and green)
            stripeBoundaries[2][i] = top - 2 * rectHeight + secondaryWave + microWave;
            
            // Bottom boundary (fixed to flag bottom with slight wave)
            stripeBoundaries[3][i] = bottom + secondaryWave * 0.3f + microWave;
        }
        
        // Draw the three stripes using the pre-calculated boundaries
        float[][] colors = {
            {1.0f, 0.6f, 0.2f}, // Saffron
            {1.0f, 1.0f, 1.0f}, // White  
            {0.0f, 0.5f, 0.0f}  // Green
        };
        
        for (int stripe = 0; stripe < 3; stripe++) {
            glColor4f(colors[stripe][0], colors[stripe][1], colors[stripe][2], flagOpacity);
            
            // Draw the stripe using the exact boundary positions
            glBegin(GL_TRIANGLE_STRIP);
            for (int i = 0; i <= segments; i++) {
                float x = left + i * segmentWidth;
                float topY = stripeBoundaries[stripe][i];
                float bottomY = stripeBoundaries[stripe + 1][i];
                
                glVertex2f(x, topY);
                glVertex2f(x, bottomY);
            }
            glEnd();
            
            // Draw a slight overlap to ensure no gaps (using quads for perfect coverage)
            if (stripe < 2) {
                // Create a very thin overlapping region
                float[] currentColor = colors[stripe];
                float[] nextColor = colors[stripe + 1];
                
                glBegin(GL_QUADS);
                for (int i = 0; i < segments; i++) {
                    float x1 = left + i * segmentWidth;
                    float x2 = left + (i + 1) * segmentWidth;
                    
                    float boundary1 = stripeBoundaries[stripe + 1][i];
                    float boundary2 = stripeBoundaries[stripe + 1][i + 1];
                    
                    // Current stripe color on top
                    glColor4f(currentColor[0], currentColor[1], currentColor[2], flagOpacity);
                    glVertex2f(x1, boundary1 + 0.003f);
                    glVertex2f(x2, boundary2 + 0.003f);
                    
                    // Next stripe color on bottom
                    glColor4f(nextColor[0], nextColor[1], nextColor[2], flagOpacity);
                    glVertex2f(x2, boundary2 - 0.003f);
                    glVertex2f(x1, boundary1 - 0.003f);
                }
                glEnd();
            }
        }
        
        // Draw the animated Ashoka Chakra in the center of white stripe
        drawAnimatedAshokachakra(centerX, centerY, rectHeight * 0.4f, time);
        
        // Add sparkle effects around the flag
        drawSparkles(time);
    }

    
    private void drawAnimatedAshokachakra(float centerX, float centerY, float radius, float time) {
        int spokes = 24;
        int circleSegments = 60;
        
        // Animated chakra with pulsing effect
        float pulseScale = 1.0f + 0.1f * (float)Math.sin(time * 2.0f);
        float animatedRadius = radius * pulseScale;
        
        // Set color to navy blue with animated intensity
        float intensity = 0.8f + 0.2f * (float)Math.sin(time * 3.0f);
        glColor4f(0.0f, 0.0f, 0.5f * intensity, flagOpacity);
        
        // Save current matrix
        glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);
        glRotatef(chakraRotation, 0.0f, 0.0f, 1.0f);
        
        // Draw outer circle with glow effect
        for (int glow = 3; glow >= 1; glow--) {
            float glowRadius = animatedRadius + glow * 0.01f;
            float glowAlpha = (0.3f / glow) * flagOpacity;
            
            glColor4f(0.0f, 0.0f, 0.5f * intensity, glowAlpha);
            glLineWidth(2.0f + glow);
            
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < circleSegments; i++) {
                float angle = 2.0f * (float)Math.PI * i / circleSegments;
                float x = glowRadius * (float)Math.cos(angle);
                float y = glowRadius * (float)Math.sin(angle);
                glVertex2f(x, y);
            }
            glEnd();
        }
        
        // Main outer circle
        glColor4f(0.0f, 0.0f, 0.5f * intensity, flagOpacity);
        glLineWidth(3.0f);
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = animatedRadius * (float)Math.cos(angle);
            float y = animatedRadius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Draw inner circle (hub) with pulsing effect
        float innerRadius = animatedRadius * 0.15f;
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = innerRadius * (float)Math.cos(angle);
            float y = innerRadius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Fill inner circle
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0.0f, 0.0f); // Center point
        for (int i = 0; i <= circleSegments; i++) {
            float angle = 2.0f * (float)Math.PI * i / circleSegments;
            float x = innerRadius * (float)Math.cos(angle);
            float y = innerRadius * (float)Math.sin(angle);
            glVertex2f(x, y);
        }
        glEnd();
        
        // Draw 24 spokes with varying intensity
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        for (int i = 0; i < spokes; i++) {
            float angle = 2.0f * (float)Math.PI * i / spokes;
            
            // Varying spoke intensity for shimmer effect
            float spokeIntensity = intensity + 0.3f * (float)Math.sin(time * 4.0f + i * 0.5f);
            glColor4f(0.0f, 0.0f, 0.5f * spokeIntensity, flagOpacity);
            
            // Inner point (from hub edge)
            float innerX = innerRadius * (float)Math.cos(angle);
            float innerY = innerRadius * (float)Math.sin(angle);
            
            // Outer point (to outer circle)
            float outerX = animatedRadius * (float)Math.cos(angle);
            float outerY = animatedRadius * (float)Math.sin(angle);
            
            glVertex2f(innerX, innerY);
            glVertex2f(outerX, outerY);
        }
        glEnd();
        
        glPopMatrix();
    }
    
    private void drawSparkles(float time) {
        // Draw animated sparkles around the flag
        glPointSize(2.0f);
        glBegin(GL_POINTS);
        
        for (int i = 0; i < 20; i++) {
            // Create pseudo-random positions based on index and time
            float x = 0.8f * (float)Math.sin(time * 0.8f + i * 2.0f);
            float y = 0.6f * (float)Math.cos(time * 0.6f + i * 1.5f);
            
            // Twinkling alpha
            float alpha = (0.5f + 0.5f * (float)Math.sin(time * 5.0f + i * 3.0f)) * flagOpacity;
            
            // Color variation
            if (i % 3 == 0) {
                glColor4f(1.0f, 0.8f, 0.0f, alpha); // Golden
            } else if (i % 3 == 1) {
                glColor4f(1.0f, 1.0f, 1.0f, alpha); // White
            } else {
                glColor4f(0.0f, 0.8f, 1.0f, alpha); // Light blue
            }
            
            glVertex2f(x, y);
        }
        glEnd();
    }
    
    public static void main(String[] args) {
        new AnimatedIndianFlag().run();
    }
}

/*
 * Animated Indian Flag Features:
 * 
 * 1. Waving Flag Effect: The flag appears to wave in the wind with sine wave distortion
 * 2. Rotating Chakra: The Ashoka Chakra rotates continuously with a pulsing glow effect
 * 3. Fade-in Animation: The flag gradually appears with opacity animation
 * 4. Animated Background: Subtle gradient background with color variations
 * 5. Sparkle Effects: Twinkling stars around the flag
 * 6. Glow Effects: The chakra has a glowing outline
 * 7. Shadow Effects: Subtle shadows on the flag stripes
 * 
 * Controls:
 * - ESC: Exit the application
 * - R: Reset the animation
 * 
 * JBang Usage:
 * 1. Save this file as AnimatedIndianFlag.java
 * 2. Run: jbang AnimatedIndianFlag.java
 */