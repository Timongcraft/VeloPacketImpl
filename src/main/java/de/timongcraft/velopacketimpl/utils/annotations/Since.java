package de.timongcraft.velopacketimpl.utils.annotations;

import com.velocitypowered.api.network.ProtocolVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks code as valid only from (including) a specified Minecraft version onwards.
 *
 * <p>Note: This annotation does not guarantee that the element was introduced in this
 * version and does not exist in earlier versions. Rather, it indicates that the element is
 * guaranteed to exist starting from the specified version onwards.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Since {

    ProtocolVersion value();

}
