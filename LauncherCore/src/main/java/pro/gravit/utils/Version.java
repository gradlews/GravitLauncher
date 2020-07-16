package pro.gravit.utils;

import java.util.*;

public final class Version {

    public static final int MAJOR = 5;
    public static final int MINOR = 1;
    public static final int PATCH = 7;
    public static final int BUILD = 1;
    public static final Version.Type RELEASE = Type.STABLE;
    public final int major;
    public final int minor;
    public final int patch;
    public final int build;
    public final Type release;


    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        build = 0;
        release = Type.UNKNOWN;
    }


    public Version(int major, int minor, int patch, int build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
        release = Type.UNKNOWN;
    }


    public Version(int major, int minor, int patch, int build, Type release) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
        this.release = release;
    }

    public static Version getVersion() {
        return new Version(MAJOR, MINOR, PATCH, BUILD, RELEASE);
    }

    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version that = (Version) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch &&
                build == that.build;
    }


    public String getVersionString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override

    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }


    public String getReleaseStatus() {
        if (release.equals(Type.UNKNOWN)) return "";
        return release.name().toLowerCase(Locale.ENGLISH);
    }

    @Override

    public String toString() {
        return String.format("%d.%d.%d-%d %s", major, minor, patch, build, getReleaseStatus());
    }


    public enum Type {
        LTS,
        STABLE,
        BETA,
        ALPHA,
        DEV,
        EXPERIMENTAL,
        UNKNOWN;

        private static final Map<String, Type> types = new HashMap<>();
        public static final Map<String, Type> unModTypes = Collections.unmodifiableMap(types);

        static {
            Arrays.asList(values()).forEach(type -> types.put(type.name().substring(0, type.name().length() < 3 ? type.name().length() : 3).toLowerCase(Locale.ENGLISH), type));
        }
    }
}
