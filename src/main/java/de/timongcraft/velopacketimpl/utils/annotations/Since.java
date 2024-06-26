package de.timongcraft.velopacketimpl.utils.annotations;

import com.velocitypowered.api.network.ProtocolVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks code as valid only from (including) a specified Minecraft version onwards
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Since {

    ProtocolVersion value();

}
