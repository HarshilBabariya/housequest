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


EMBLEMS = {
    "diwali": p("#FFF59E0B", "M60,40 Q69,50 78,40 Z") + p("#FFFF7A1A", "M69,39.5 Q65,34 69,27 Q73,34 69,39.5 Z") + p("#FFFFF59D", "M69,38 Q67.5,35 69,31.5 Q70.5,35 69,38 Z"),
    "holi": p("#FFEC4899", circle(69, 39, 7)) + p("#FFFACC15", circle(77, 33, 3)) + p("#FF22C55E", circle(62, 31, 2.6)) + p("#FF3B82F6", circle(78, 45, 2.2)),
    "navratri": f'    <path android:strokeColor="#FFFACC15" android:strokeWidth="2.6" android:strokeLineCap="round" android:pathData="M62,46 L76,30 M62,30 L76,46" />\n' + p("#FFDC2626", circle(62, 46, 1.8)) + p("#FF16A34A", circle(76, 46, 1.8)),
    "uttarayan": p("#FFEF4444", "M70,26 L77,34 L70,43 L63,34 Z") + p("#FFFACC15", "M70,26 L77,34 L70,34 Z") + '    <path android:strokeColor="#FFFFFFFF" android:strokeWidth="0.8" android:pathData="M70,43 Q74,47 69,51 Q66,54 70,57" />\n',
    "tiranga": '    <path android:strokeColor="#FF6B4F2A" android:strokeWidth="1.4" android:pathData="M63,48 L63,24" />\n' + p("#FFFF9933", "M63.7,25 h15 v4 h-15 Z") + p("#FFFFFFFF", "M63.7,29 h15 v4 h-15 Z") + p("#FF138808", "M63.7,33 h15 v4 h-15 Z") + '    <path android:strokeColor="#FF1E3A8A" android:strokeWidth="0.5" android:pathData="' + circle(71.2, 31, 1.6) + '" />\n',
    "rakhi": '    <path android:strokeColor="#FFFACC15" android:strokeWidth="1.6" android:pathData="M58,39 L80,39" />\n' + petals(69, 39, 4, 2.8, 8, "#FFF472B6", "#FFFACC15"),
    "ganesh": petals(69, 39, 4.5, 3.2, 10, "#FFF97316", "#FFFBBF24") + p("#FFDC2626", circle(69, 39, 2)),
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
    inner = "".join("    " + line + "\n" for line in (HOUSE + EMBLEMS[key]).rstrip("\n").split("\n"))
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

write(f"{RES}/values-v31/themes.xml", '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n' + "\n".join(styles) + "\n</resources>\n")
print("ok", len(PACKS))
