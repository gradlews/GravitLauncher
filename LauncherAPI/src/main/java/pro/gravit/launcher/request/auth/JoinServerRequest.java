package pro.gravit.launcher.request.auth;

import pro.gravit.launcher.LauncherNetworkAPI;
import pro.gravit.launcher.events.request.JoinServerRequestEvent;
import pro.gravit.launcher.request.Request;
import pro.gravit.launcher.request.websockets.WebSocketRequest;
import pro.gravit.utils.helper.VerifyHelper;

public final class JoinServerRequest extends Request<JoinServerRequestEvent> implements WebSocketRequest {

    // Instance
    @LauncherNetworkAPI
    private final String username;
    @LauncherNetworkAPI
    private final String accessToken;
    @LauncherNetworkAPI
    private final String serverID;


    public JoinServerRequest(String username, String accessToken, String serverID) {
        this.username = VerifyHelper.verifyUsername(username);
        this.accessToken = accessToken;
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }

    @Override
    public String getType() {
        return "joinServer";
    }
}
