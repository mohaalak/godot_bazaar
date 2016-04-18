def can_build(plat):
	return plat=="android"

def configure(env):
	if (env['platform'] == 'android'):
		env.android_module_file("android/GodotBazaar.java")
		env.android_module_file("android/util/Base64.java")
		env.android_module_file("android/util/Base64DecoderException.java")
		env.android_module_file("android/util/IabException.java")
		env.android_module_file("android/util/IabHelper.java")
		env.android_module_file("android/util/IabResult.java")
		env.android_module_file("android/util/Inventory.java")
		env.android_module_file("android/util/Purchase.java")
		env.android_module_file("android/util/Security.java")
		env.android_module_file("android/util/SkuDetails.java")

		env.disable_module()
