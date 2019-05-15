package com.github.travelplannerapp.di;

import com.github.travelplannerapp.login.LoginActivity;
import com.github.travelplannerapp.login.LoginModule;
import com.github.travelplannerapp.login.LoginViewModel;
import com.github.travelplannerapp.travels.TravelsActivity;
import com.github.travelplannerapp.travels.TravelsModule;
import com.github.travelplannerapp.travels.TravelsViewModel;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Binds all sub-components within the app.
 */
@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector(modules = {LoginViewModel.class, LoginModule.class})
    abstract LoginActivity bindLoginActivity();

    @ContributesAndroidInjector(modules = {TravelsViewModel.class, TravelsModule.class})
    abstract TravelsActivity bindTravelsActivity();

    // Add bindings for other sub-components here
}