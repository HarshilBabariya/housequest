import math
import os
import sys

RES = sys.argv[1]

HOUSE = '''    <group android:translateY="3">
        <path android:fillColor="#2E000000" android:pathData="M54,30 L78,51 Q80,53 77,54 L72,54 L72,76 Q72,80 68,80 L40,80 Q36,80 36,76 L36,54 L31,54 Q28,53 30,51 Z" />
    </group>
    <path android:fillColor="#FFFFFFFF" android:pathData="M36,54 L72,54 L72,76 Q72,80 68,80 L40,80 Q36,80 36,76 Z" />
    <path android:fillColor="#FFFFF3EE" android:pathData="M54,30 L78,51 Q80,53 77,54 L31,54 Q28,53 30,51 Z" />
    <path android:fillColor="#FFE8386B" android:pathData="M49.5,62 h9 a2.5,2.5 0 0,1 2.5,2.5 V80 H47 V64.5 a2.5,2.5 0 0,1 2.5,-2.5 Z" />
    <path android:fillColor="#FFFFE38A" android:pathData="M57,72 m-1.1,0 a1.1,1.1 0 1,0 2.2,0 a1.1,1.1 0 1,0 -2.2,0" />
    <path android:fillColor="#FFFFD2C2" android:pathData="M40.8,60 h3.4 a1.3,1.3 0 0,1 1.3,1.3 v3.4 a1.3,1.3 0 0,1 -1.3,1.3 h-3.4 a1.3,1.3 0 0,1 -1.3,-1.3 v-3.4 a1.3,1.3 0 0,1 1.3,-1.3 Z M63.8,60 h3.4 a1.3,1.3 0 0,1 1.3,1.3 v3.4 a1.3,1.3 0 0,1 -1.3,1.3 h-3.4 a1.3,1.3 0 0,1 -1.3,-1.3 v-3.4 a1.3,1.3 0 0,1 1.3,-1.3 Z" />
'''


def circle(x, y, r):
    return f"M{x},{y} m-{r},0 a{r},{r} 0 1,0 {2 * r},0 a{r},{r} 0 1,0 -{2 * r},0"


def p(fill, d, extra=""):
    return f'    <path android:fillColor="{fill}" android:pathData="{d}"{extra} />\n'


def rays(x, y, r1, r2, n=8, rot=0):
    out = []
    for i in range(n):
        a = rot + i * 2 * math.pi / n
        out.append(f"M{x + math.cos(a) * r1:.2f},{y + math.sin(a) * r1:.2f} L{x + math.cos(a) * r2:.2f},{y + math.sin(a) * r2:.2f}")
    return " ".join(out)


def petals(x, y, r, pr, n, color, center):
    s = "".join(p(color, circle(round(x + math.cos(i * 2 * math.pi / n) * r, 2), round(y + math.sin(i * 2 * math.pi / n) * r, 2), pr)) for i in range(n))
    return s + p(center, circle(x, y, pr * 0.9))


def ellipse(x, y, rx, ry):
    return f"M{x - rx},{y} a{rx},{ry} 0 1,0 {2 * rx},0 a{rx},{ry} 0 1,0 -{2 * rx},0"


def stroke(color, width, d, fill=None):
    fill_attr = f' android:fillColor="{fill}"' if fill else ""
    return f'    <path{fill_attr} android:strokeColor="{color}" android:strokeWidth="{width}" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="{d}" />\n'


GANESH = (
    stroke("#FFB45309", 0.8, "M24,3 l3,5 3,-3 0,6 H18 l0,-6 3,3 Z", "#FFFBBF24")
    + p("#FFDC2626", circle(24, 5.5, 1.3))
    + stroke("#FFC2410C", 1, ellipse(11, 21, 8, 10), "#FFFDBA74")
    + stroke("#FFC2410C", 1, ellipse(37, 21, 8, 10), "#FFFDBA74")
    + p("#FFFB923C", ellipse(11.5, 21, 4.5, 6.5))
    + p("#FFFB923C", ellipse(36.5, 21, 4.5, 6.5))
    + stroke("#FFC2410C", 1, "M15,16 c0,-5 4,-6 9,-6 s9,1 9,6 v6 c0,4 -3,6 -5,7 H20 c-2,-1 -5,-3 -5,-7 Z", "#FFFDBA74")
    + p("#FFF59E0B", "M17.7,10.6 h12.6 a1.2,1.2 0 0,1 1.2,1.2 v0.2 a1.2,1.2 0 0,1 -1.2,1.2 h-12.6 a1.2,1.2 0 0,1 -1.2,-1.2 v-0.2 a1.2,1.2 0 0,1 1.2,-1.2 Z")
    + stroke("#FFDC2626", 1.6, "M24,13.5 v4")
    + stroke("#FFDC2626", 1, "M21,18.5 h6")
    + stroke("#FF3B1D0A", 1.2, "M18.5,21.3 q1.5,-1.2 3,0 M25.5,21.3 q1.5,-1.2 3,0")
    + stroke("#FFFFF7ED", 1.8, "M28.5,26.5 q2.5,1 3.5,-0.5")
    + stroke("#FFFDBA74", 5, "M24,24 c0,7 0,11 -3.5,13.5 S14,40 14.5,37 s4,-2 4.5,0.5")
    + stroke("#FFC2410C", 0.9, "M22,28 h4 M22,31 h4 M21.5,34 h4")
    + stroke("#FFB45309", 0.8, "M34,32 c1.5,2 5,4.5 5,7.5 a5,3.5 0 0,1 -10,0 c0,-3 3.5,-5.5 5,-7.5 Z", "#FFFFF7ED")
    + stroke("#FFD97706", 0.6, "M34,33 v9.5 M31.5,36 q-1,2.5 -0.3,5.5 M36.5,36 q1,2.5 0.3,5.5")
    + p("#FF16A34A", circle(21, 9, 0.9))
    + p("#FF16A34A", circle(27, 9, 0.9))
)


def indent(text):
    return "".join("    " + line + "\n" for line in text.rstrip("\n").split("\n"))


EMBLEMS = {
    "diwali": p("#FFF59E0B", "M60,40 Q69,50 78,40 Z") + p("#FFFF7A1A", "M69,39.5 Q65,34 69,27 Q73,34 69,39.5 Z") + p("#FFFFF59D", "M69,38 Q67.5,35 69,31.5 Q70.5,35 69,38 Z"),
    "holi": p("#FFEC4899", circle(69, 39, 7)) + p("#FFFACC15", circle(77, 33, 3)) + p("#FF22C55E", circle(62, 31, 2.6)) + p("#FF3B82F6", circle(78, 45, 2.2)),
    "navratri": f'    <path android:strokeColor="#FFFACC15" android:strokeWidth="2.6" android:strokeLineCap="round" android:pathData="M62,46 L76,30 M62,30 L76,46" />\n' + p("#FFDC2626", circle(62, 46, 1.8)) + p("#FF16A34A", circle(76, 46, 1.8)),
    "uttarayan": p("#FFEF4444", "M70,26 L77,34 L70,43 L63,34 Z") + p("#FFFACC15", "M70,26 L77,34 L70,34 Z") + '    <path android:strokeColor="#FFFFFFFF" android:strokeWidth="0.8" android:pathData="M70,43 Q74,47 69,51 Q66,54 70,57" />\n',
    "tiranga": '    <path android:strokeColor="#FF6B4F2A" android:strokeWidth="1.4" android:pathData="M63,48 L63,24" />\n' + p("#FFFF9933", "M63.7,25 h15 v4 h-15 Z") + p("#FFFFFFFF", "M63.7,29 h15 v4 h-15 Z") + p("#FF138808", "M63.7,33 h15 v4 h-15 Z") + '    <path android:strokeColor="#FF1E3A8A" android:strokeWidth="0.5" android:pathData="' + circle(71.2, 31, 1.6) + '" />\n',
    "rakhi": '    <path android:strokeColor="#FFFACC15" android:strokeWidth="1.6" android:pathData="M58,39 L80,39" />\n' + petals(69, 39, 4, 2.8, 8, "#FFF472B6", "#FFFACC15"),
    "ganesh": '    <group android:translateX="58" android:translateY="25.5" android:scaleX="0.46" android:scaleY="0.46">\n' + indent(GANESH) + "    </group>\n",
    "christmas": p("#FFDC2626", "M40,40 Q52,22 70,24 Q80,26 82,34 Q74,30 70,33 L68,40 Z") + p("#FFFFFFFF", "M38,38 h32 a2,2 0 0,1 0,5 h-32 a2,2 0 0,1 0,-5 Z") + p("#FFFFFFFF", circle(82, 35, 3)),
    "new_year": p("#FFF5C451", "M69,30 L71.6,36.4 L78.5,36.9 L73.2,41.3 L74.9,48 L69,44.3 L63.1,48 L64.8,41.3 L59.5,36.9 L66.4,36.4 Z"),
}

PACKS = {
    "diwali": ("DIWALI", "#FF7F1D1D", "#FFF59E0B", ["#FFFFC107", "#FFFF7043", "#FFFFF59D"], "#FF1A0B07"),
    "holi": ("HOLI", "#FFE91E8C", "#FF00B8D4", ["#FFFACC15", "#FF22C55E", "#FFFFFFFF"], "#FF1A0A16"),
    "navratri": ("NAVRATRI", "#FFD9271C", "#FFFF9F1C", ["#FFFACC15", "#FF16A34A", "#FFFFFFFF"], "#FF1A0C09"),
    "uttarayan": ("UTTARAYAN", "#FF0284C7", "#FF7DD3FC", ["#FFFACC15", "#FFEF4444", "#FFFFFFFF"], "#FF07131F"),
    "tiranga": ("TIRANGA", "#FFFF9933", "#FF138808", ["#FFFFFFFF", "#FFFF9933", "#FF138808"], "#FF0E1210"),
    "rakhi": ("RAKHI", "#FFDB2777", "#FFF9A8D4", ["#FFFACC15", "#FFFFFFFF", "#FFFB923C"], "#FF1C0E14"),
    "ganesh": ("GANESH", "#FFEA580C", "#FFFACC15", ["#FFDC2626", "#FFFFFFFF", "#FFFBBF24"], "#FF1C0F06"),
    "christmas": ("CHRISTMAS", "#FFC62828", "#FF2E7D32", ["#FFFFFFFF", "#FFFFFFFF", "#FFD4A017"], "#FF0D1410"),
    "new_year": ("NEW_YEAR", "#FF312E81", "#FF7C3AED", ["#FFF5C451", "#FFE5E7EB", "#FFF472B6"], "#FF0C0A1F"),
}

BURSTS = [(30, 34, 0), (26, 66, 150), (82, 70, 300)]


def background(start, end):
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0 H108 V108 H0 Z">
        <aapt:attr name="android:fillColor">
            <gradient android:startX="0" android:startY="0" android:endX="108" android:endY="108" android:type="linear">
                <item android:offset="0" android:color="{start}" />
                <item android:offset="1" android:color="{end}" />
            </gradient>
        </aapt:attr>
    </path>
    <path android:fillColor="#1AFFFFFF" android:pathData="{circle(92, 14, 34)}" />
    <path android:fillColor="#14000000" android:pathData="{circle(14, 96, 30)}" />
</vector>
'''


def burst_group(i, x, y, color, rot):
    return f'''    <group android:name="burst{i}" android:pivotX="{x}" android:pivotY="{y}" android:scaleX="0.8" android:scaleY="0.8">
        <path android:name="rays{i}" android:strokeColor="{color}" android:strokeWidth="1.6" android:strokeLineCap="round" android:pathData="{rays(x, y, 3, 8.5, 8, math.radians(rot))}" />
        <path android:fillColor="{color}" android:pathData="{circle(x, y, 1.4)}" />
    </group>
'''


def foreground(key, colors):
    sparks = "".join(burst_group(i, x, y, colors[i % len(colors)], rot) for i, (x, y, rot) in enumerate(BURSTS))
    inner = indent(HOUSE + EMBLEMS[key])
    body = f'    <group android:name="emblem" android:pivotX="54" android:pivotY="54">\n{inner}    </group>\n'
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
{body}{sparks}</vector>
'''


def animator(prop, frm, to, duration):
    return f'<objectAnimator android:propertyName="{prop}" android:valueFrom="{frm}" android:valueTo="{to}" android:valueType="floatType" android:duration="{duration}" />'


def target(name, animators):
    inner = "\n".join(f"                {a}" for a in animators)
    return f'''    <target android:name="{name}">
        <aapt:attr name="android:animation">
            <set xmlns:android="http://schemas.android.com/apk/res/android">
{inner}
            </set>
        </aapt:attr>
    </target>
'''


def splash(key):
    targets = target("emblem", [animator("scaleX", 0.85, 1, 500), animator("scaleY", 0.85, 1, 500)])
    for i in range(len(BURSTS)):
        targets += target(f"burst{i}", [animator("scaleX", 0.8, 1.35, 900), animator("scaleY", 0.8, 1.35, 900)])
        targets += target(f"rays{i}", [animator("strokeAlpha", 1, 0, 900)])
    return f'''<?xml version="1.0" encoding="utf-8"?>
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:drawable="@drawable/ic_launcher_foreground_{key}">
{targets}</animated-vector>
'''


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


styles = []
for key, (enum, start, end, colors, dark) in PACKS.items():
    write(f"{RES}/drawable/ic_launcher_background_{key}.xml", background(start, end))
    write(f"{RES}/drawable/ic_launcher_foreground_{key}.xml", foreground(key, colors))
    write(f"{RES}/drawable/splash_{key}.xml", splash(key))
    write(f"{RES}/mipmap-anydpi-v26/ic_launcher_{key}.xml", f'''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background_{key}" />
    <foreground android:drawable="@drawable/ic_launcher_foreground_{key}" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
''')
    name = "".join(part.capitalize() for part in key.split("_"))
    styles.append(f'''    <style name="Theme.HomeQuest.Splash.{name}" parent="Theme.HomeQuest">
        <item name="android:windowSplashScreenBackground">{dark}</item>
        <item name="android:windowSplashScreenIconBackgroundColor">{start}</item>
        <item name="android:windowSplashScreenAnimatedIcon">@drawable/splash_{key}</item>
    </style>''')

write(f"{RES}/drawable/ic_ganesh.xml", f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
{GANESH}</vector>
''')
write(f"{RES}/values-v31/themes.xml", '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n' + "\n".join(styles) + "\n</resources>\n")
print("ok", len(PACKS))
