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
        )
    )
)
)
