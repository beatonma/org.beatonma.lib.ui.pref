# Preferences
## org.beatonma.lib.ui.pref

**This module is not intended for 3rd party use**, although you are free to use any parts of it
you like (with attribution). Be aware that APIs are subject to change without notice and
support will be limited.

This module is part of a larger group of libraries. The parent project `build.gradle` holds most
of the build configuration but is not available publicly. If you want to build this module
you will need to write your own `build.gradle` configuration. The dependencies you need to
include can be found listed in `dependencies.txt`.

----

This module provides a framework for handling SharedPreferences in Android.
 - preference definitions in JSON format
 - preferences can be dependent on one another
    - e.g. only show this preference if `some_preference == false` / `some_preference != 0` / whatever
 - preferences may contain other nested preferences
 - custom built UI for each preference type
 - easily extensible with new preference types

See class comments in `org.beatonma.lib.ui.pref.preferences` for full details on formatting and
available parameters. See `example_preferences.json` for an example.
