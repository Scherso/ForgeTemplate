package dev.example.template;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ForgeTemplate.ID, name = ForgeTemplate.NAME, version = ForgeTemplate.VER)
public class ForgeTemplate {

    public static final String NAME = "@NAME@", VER = "@VERSION@", ID = "@ID@";

    @Mod.Instance(ID)
    public static ForgeTemplate Instance;

    @Mod.EventHandler
    protected void onInitialization(FMLInitializationEvent event) {
        System.out.println("Hello, World");
    }

}
