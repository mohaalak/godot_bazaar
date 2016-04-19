def can_build(plat):
	return plat=="android"

def configure(env):
	if (env['platform'] == 'android'):
		env.android_add_java_dir("android/GodotBazaar.java")
		env.android_add_java_dir("android/util")
		env.disable_module()
