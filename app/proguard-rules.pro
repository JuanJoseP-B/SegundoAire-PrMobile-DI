# Reglas de R8 del build de release.
#
# No hay reglas keep propias, y es intencional. Se comprobó en la salida de R8
# (app/build/outputs/mapping/release/configuration.txt y seeds.txt) que todo lo
# que se carga por nombre ya queda cubierto:
#
#  - Servicio de accesibilidad, MainActivity y SegundoAireApp: AAPT genera su
#    -keep a partir del AndroidManifest.xml.
#  - Room: la librería trae `-keep class * extends androidx.room.RoomDatabase`,
#    así que AppDatabase_Impl (cargada por reflexión) se conserva.
#  - Hilt/Dagger: genera código sin reflexión y las clases de entrada
#    (@AndroidEntryPoint) salen del manifiesto.
#  - DataStore Preferences: sus reglas de protobuf vienen con la librería.
#  - Overlay (OverlayWindowManager, OverlayLifecycleOwner): no se resuelven por
#    nombre, R8 puede renombrarlas sin riesgo.
#
# Si se agrega algo que se cargue por reflexión, o una librería sin reglas de
# consumidor, agrégalo aquí con la razón.

# Trazas legibles: sin esto, los crashes de release salen como "Unknown Source".
# El mapping.txt de cada build permite des-ofuscarlas con retrace.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
