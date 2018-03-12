/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fuelgauge.batterytip.detectors;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import android.content.Context;

import com.android.settings.fuelgauge.batterytip.AnomalyDatabaseHelper;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.testutils.DatabaseTestUtils;
import com.android.settings.testutils.SettingsRobolectricTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

@RunWith(SettingsRobolectricTestRunner.class)
public class RestrictAppDetectorTest {

    private static final String PACKAGE_NAME = "com.android.app";
    private Context mContext;
    private BatteryTipPolicy mPolicy;
    private RestrictAppDetector mRestrictAppDetector;
    private List<AppInfo> mAppInfoList;
    @Mock
    private BatteryDatabaseManager mBatteryDatabaseManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mAppInfoList = new ArrayList<>();
        mAppInfoList.add(new AppInfo.Builder().setPackageName(PACKAGE_NAME).build());

        mContext = RuntimeEnvironment.application;
        mPolicy = spy(new BatteryTipPolicy(mContext));
        mRestrictAppDetector = new RestrictAppDetector(mContext, mPolicy);
        mRestrictAppDetector.mBatteryDatabaseManager = mBatteryDatabaseManager;
    }

    @After
    public void cleanUp() {
        DatabaseTestUtils.clearDb(mContext);
    }

    @Test
    public void testDetect_hasAnomaly_tipNew() {
        doReturn(mAppInfoList).when(mBatteryDatabaseManager)
            .queryAllAnomalies(anyLong(), eq(AnomalyDatabaseHelper.State.NEW));

        assertThat(mRestrictAppDetector.detect().getState()).isEqualTo(BatteryTip.StateType.NEW);
    }

    @Test
    public void testDetect_hasAutoHandledAnomaly_tipHandled() {
        doReturn(new ArrayList<AppInfo>()).when(mBatteryDatabaseManager)
            .queryAllAnomalies(anyLong(), eq(AnomalyDatabaseHelper.State.NEW));
        doReturn(mAppInfoList).when(mBatteryDatabaseManager)
            .queryAllAnomalies(anyLong(), eq(AnomalyDatabaseHelper.State.AUTO_HANDLED));

        assertThat(mRestrictAppDetector.detect().getState())
            .isEqualTo(BatteryTip.StateType.HANDLED);
    }

    @Test
    public void testDetect_noAnomaly_tipInvisible() {
        doReturn(new ArrayList<AppInfo>()).when(mBatteryDatabaseManager)
            .queryAllAnomalies(anyLong(), anyInt());

        assertThat(mRestrictAppDetector.detect().getState())
            .isEqualTo(BatteryTip.StateType.INVISIBLE);
    }

    @Test
    public void testUseFakeData_alwaysFalse() {
        assertThat(RestrictAppDetector.USE_FAKE_DATA).isFalse();
    }
}