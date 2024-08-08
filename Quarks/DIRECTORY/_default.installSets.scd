(
'uninstall main': (
    main: (
        'sc-hacks-redux-gitquark': false
    )
),

default: (
    main: (
        'sc-hacks-redux-gitquark': true
    )
    ,
    submodules: (
        'team.core': (
            // GuiHelpers: true
            // Logln: true
            ,notification: true
            // ,PopUpTreeMenu2: true
            // ,StringUtils: true
            // ,mcFormat: true
            // ,UserPrefs: true
        )
    )
),

'| uninstall all ! this may break other projects |': (
    // startupFiles: (
    //     removeIfSymlinkEqualsThis: ["startupFiles" +/+ "startup.scd"]
    // ),
    main: (
        'sc-hacks-redux': false
    )
    ,
    submodules: (
        'team.core': (
    //         GuiHelpers: false
    //         ,Logln: false
            ,notification: false
    //         ,PopUpTreeMenu2: false
    //         ,StringUtils: false
    //         ,mcFormat: false
    //         ,UserPrefs: false
        )
    )
)
)
