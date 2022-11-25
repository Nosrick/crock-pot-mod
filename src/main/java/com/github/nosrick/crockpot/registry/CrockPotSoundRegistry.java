package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public enum CrockPotSoundRegistry {

    CROCK_POT_BUBBLE("block.crock_pot_bubble"),
    CROCK_POT_BOIL("block.crock_pot_boil"),
    CROCK_POT_SPLASH("block.crock_pot_splash");

    private SoundEvent soundEvent;
    private final Identifier identifier;

    //TODO: Wait for sound registry to get fixed

    CrockPotSoundRegistry(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
        this.identifier = soundEvent.getId();
    }

    CrockPotSoundRegistry(String identifier) {
        this.identifier = new Identifier(CrockPotMod.MOD_ID, identifier);
        this.soundEvent = null;//(this.identifier);
    }

    CrockPotSoundRegistry(String identifier, SoundEvent soundEvent) {
        this.identifier = new Identifier(CrockPotMod.MOD_ID, identifier);
        this.soundEvent = soundEvent;
    }

    public SoundEvent get() {
        if(this.soundEvent == null && this.identifier != null) {
            this.soundEvent = null; //(this.identifier);
        }

        return this.soundEvent;
    }

    public static void registerAll() {
        for(CrockPotSoundRegistry value : values()) {
            Registry.register(
                    Registries.SOUND_EVENT,
                    value.identifier,
                    value.soundEvent);
        }
    }
}
