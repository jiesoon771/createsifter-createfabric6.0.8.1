package io.github.shulej.createsifter.foundation.util;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;

import io.github.shulej.createsifter.CreateSifter;

public class ModLang extends Lang {
	public ModLang() {
		super();
	}
	public static LangBuilder builder() {
		return new LangBuilder(CreateSifter.MODID);
	}
	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
}
