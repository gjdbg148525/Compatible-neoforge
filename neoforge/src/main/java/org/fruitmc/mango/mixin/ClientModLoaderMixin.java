package org.fruitmc.mango.mixin;

import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import org.fruitmc.mango.MangoNeoForge;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("UnstableApiUsage")
@Mixin({ClientModLoader.class})
public class ClientModLoaderMixin {

    @Redirect(method = {"finish"}, at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/earlydisplay/DisplayWindow;close()V", ordinal = 0))
    private static void onFinish(DisplayWindow displayWindow) {
        if (MangoNeoForge.IS_VULKAN_BACKEND) {
            try {
                Field windowField = DisplayWindow.class.getDeclaredField("window");
                windowField.setAccessible(true);
                long handle = windowField.getLong(displayWindow);
                if (handle != 0L) {
                    GLFW.glfwHideWindow(handle);
                    GLFW.glfwDestroyWindow(handle);
                }

                Field closedField = DisplayWindow.class.getDeclaredField("closed");
                closedField.setAccessible(true);
                closedField.setBoolean(displayWindow, true);
                Field schedulerField = DisplayWindow.class.getDeclaredField("renderScheduler");
                schedulerField.setAccessible(true);
                ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(displayWindow);
                scheduler.shutdown();
            } catch (Exception _) {
            }

        } else {
            displayWindow.close();
        }
    }
}
