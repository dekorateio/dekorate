package io.dekorate.utils;

import io.dekorate.kubernetes.annotation.Resources;

public class ResourcesRequirement {

	public static boolean  isConfigured(Resources resources) {
		return resources != null
				&& (Strings.isNotNullOrEmpty(resources.limits().cpu())
				|| Strings.isNotNullOrEmpty(resources.limits().memory())
				|| Strings.isNotNullOrEmpty(resources.requests().memory()))
				|| Strings.isNotNullOrEmpty(resources.requests().cpu());
	}
}
