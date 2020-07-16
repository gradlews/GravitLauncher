package pro.gravit.launcher.modules;

import java.util.HashMap;
import java.util.Map;

public abstract class LauncherModule {
    protected final LauncherModuleInfo moduleInfo;
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Event>, EventHandler> eventMap = new HashMap<>();
    protected LauncherModulesManager modulesManager;
    protected ModulesConfigManager modulesConfigManager;
    protected InitStatus initStatus = InitStatus.CREATED;
    private LauncherModulesContext context;

    protected LauncherModule() {
        moduleInfo = new LauncherModuleInfo("UnknownModule");
    }

    protected LauncherModule(LauncherModuleInfo info) {
        moduleInfo = info;
    }

    public LauncherModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public InitStatus getInitStatus() {
        return initStatus;
    }

    public LauncherModule setInitStatus(InitStatus initStatus) {
        this.initStatus = initStatus;
        return this;
    }

    /**
     * The internal method used by the ModuleManager
     * DO NOT TOUCH
     *
     * @param context Private context
     */
    public void setContext(LauncherModulesContext context) {
        if (this.context != null) throw new IllegalStateException("Module already set context");
        this.context = context;
        this.modulesManager = context.getModulesManager();
        this.modulesConfigManager = context.getModulesConfigManager();
        this.setInitStatus(InitStatus.PRE_INIT_WAIT);
    }

    /**
     * This method is called before initializing all modules and resolving dependencies.
     * <b>You can</b>:
     * - Use to Module Manager
     * - Add custom modules not described in the manifest
     * - Change information about your module or modules you control
     * <b>You can not</b>:
     * - Use your dependencies
     * - Use Launcher, LaunchServer, ServerWrapper API
     * - Change the names of any modules
     */
    public void preInitAction() {
        //NOP
    }

    public LauncherModule preInit() {
        if (!initStatus.equals(InitStatus.PRE_INIT_WAIT))
            throw new IllegalStateException("PreInit not allowed in current state");
        initStatus = InitStatus.PRE_INIT;
        preInitAction();
        initStatus = InitStatus.INIT_WAIT;
        return this;
    }

    /**
     * Basic module initialization method
     * <b>You can</b>:
     * - Subscribe to events
     * - Use your dependencies
     * - Use provided initContext
     * - Receive modules and access the module’s internal methods
     * <b>You can not</b>:
     * - Modify module description, dependencies
     * - Add modules
     * - Read configuration
     *
     * @param initContext <b>null</b> on module initialization during boot or startup
     *                    Not <b>null</b> during module initialization while running
     */
    public abstract void init(LauncherInitContext initContext);

    /**
     * Registers an event handler for the current module
     *
     * @param handle your event handler
     * @param tClass event class
     * @param <T>    event type
     * @return true if adding a handler was successful
     */
    protected <T extends Event> boolean registerEvent(EventHandler<T> handle, Class<T> tClass) {
        eventMap.put(tClass, handle);
        return true;
    }

    /**
     * Call the handler of the current module
     *
     * @param event event handled
     * @param <T>   event type
     */
    @SuppressWarnings("unchecked")
    public final <T extends Event> void callEvent(T event) {
        Class<? extends Event> tClass = event.getClass();
        for (@SuppressWarnings("rawtypes") Map.Entry<Class<? extends Event>, EventHandler> e : eventMap.entrySet()) {

            if (e.getKey().isAssignableFrom(tClass)) {
                e.getValue().event(event);
                if (event.isCancel()) return;
            }
        }
    }

    /**
     * Module initialization status at the current time
     */
    public enum InitStatus {
        /**
         * When creating an object
         */
        CREATED(false),
        /**
         * After setting the context
         */
        PRE_INIT_WAIT(true),
        /**
         * During the pre-initialization phase
         */
        PRE_INIT(false),
        /**
         * Awaiting initialization phase
         */
        INIT_WAIT(true),
        /**
         * During the initialization phase
         */
        INIT(false),
        FINISH(true);

        private final boolean isAvailable;

        InitStatus(boolean b) {
            isAvailable = b;
        }

        public boolean isAvailable() {
            return isAvailable;
        }
    }


    @FunctionalInterface
    public interface EventHandler<T extends Event> {
        void event(T e);
    }

    public static class Event {
        protected boolean cancel = false;

        public boolean isCancel() {
            return cancel;
        }

        public Event cancel() {
            this.cancel = true;
            return this;
        }
    }
}
