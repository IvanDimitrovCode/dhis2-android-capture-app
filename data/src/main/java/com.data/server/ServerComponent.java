package com.data.server;

import android.support.annotation.NonNull;

import com.data.dagger.PerServer;
import com.data.user.UserComponent;
import com.data.user.UserModule;

import dagger.Subcomponent;

@PerServer
@Subcomponent(modules = ServerModule.class)
public interface ServerComponent {

    @NonNull
    UserManager userManager();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);
}
