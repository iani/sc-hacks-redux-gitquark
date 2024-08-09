(
'uninstall main': (
    main: (
        'sc-hacks-redux-gitquark': false
    )
),

defaultPlusBabel: (

    main: (
        'sc-hacks-redux-gitquark': true
    )
    ,
    submodules: (
        'notification': (
            notification: true
        ),
        'team.core': (
            GuiHelpers: false
            ,Logln: true
            ,Notification: false
            ,PopUpTreeMenu2: false
            ,StringUtils: false
            ,mcFormat: true
            ,UserPrefs: true
            ,OrgBabel: true
            ,Emacs_sclang_extensions: true
        ))
)

default: (
    main: (
        'sc-hacks-redux-gitquark': true
    )
    ,
    submodules: (
        'notification': (
            notification: true
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
        'notification': (
            notification: false
        ),
        'team.core': (
            GuiHelpers: false
            ,Logln: false
            ,Notification: false
            ,PopUpTreeMenu2: false
            ,StringUtils: false
            ,mcFormat: false
            ,UserPrefs: false
            ,OrgBabel: false
            ,Emacs_sclang_extensions: false
        ))
)
)
