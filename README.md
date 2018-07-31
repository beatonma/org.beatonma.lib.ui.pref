# Preferences
## org.beatonma.lib.ui.pref

**This module is not intended for 3rd party use**, although you are free to use any parts of it
you like (with attribution). Be aware that APIs are subject to change without notice and
support will be limited.

This module is part of a larger group of libraries that I use via a private Artifactory repository.
It is not currently available on any public repositories.

The parent project holds most of the build configuration but is not available publicly. If you
really want to build this module you will need to write your own `build.gradle` configuration.
The dependencies you need to include can be found listed in `dependencies.txt`, but some of those
are private too so I'm afraid you will need to go through the same process for each module - like
I said, not intended for 3rd party use!

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

### Real examples
This library is used in IO18 Clock for Android (not yet released)
