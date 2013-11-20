/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.shell.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class WGet {

    public static boolean wget(String theUrl, File file) {

        URLConnection con;
        try {
            URL gotoUrl = new URL(theUrl);
            con = gotoUrl.openConnection();
            con.connect();
            String type = con.getContentType();

            if (type != null) {
                System.out.println("wgetting " + theUrl);

                byte[] buffer = new byte[4 * 1024];
                int read;

                FileOutputStream os = new FileOutputStream(file);
                InputStream in = con.getInputStream();

                while ((read = in.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }

                os.close();
                in.close();
                System.out.println("got " + file + " size " + read + " bytes");
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.err.println("failed to wget " + theUrl + " " + e);
            return false;
        }

    }
}
