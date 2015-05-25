package com.zuoxiaolong.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.zuoxiaolong.config.Configuration;

/*
 * Copyright 2002-2015 the original author or authors.
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

/**
 * @author 左潇龙
 * @since 2015年5月26日 上午1:14:47
 */
public abstract class HtmlPageDao extends BaseDao {
	
	public static void flush() {
		String contextPath = Configuration.isProductEnv() ? Configuration.get("context.path.product") : Configuration.get("context.path");
		File[] htmlFiles = new File(Configuration.getContextPath("html")).listFiles();
		List<String> htmlPageList = new ArrayList<String>();
		htmlPageList.add(contextPath);
		for (int i = 0; i < htmlFiles.length; i++) {
			htmlPageList.add(contextPath + "/html/" + htmlFiles[i].getName());
		}
		for (String url : htmlPageList) {
			save(url);
		}
	}

	public static boolean save(final String url) {
        return execute(new TransactionalOperation<Boolean>() {
            @Override
            public Boolean doInConnection(Connection connection) {
                try {
                	PreparedStatement findStatement = connection.prepareStatement("select id from html_page where url=?");
                	findStatement.setString(1, url);
                	if (findStatement.executeQuery().next()) {
						return true;
					}
                	PreparedStatement saveStatement = connection.prepareStatement("insert into html_page (url,is_push,push_date) values (?,?,?)");
                	saveStatement.setString(1, url);
                	saveStatement.setInt(2, 0);
                    saveStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    int result = saveStatement.executeUpdate();
                    return result > 0;
                } catch (SQLException e) {
                    error("save html_page failed ..." , e);
                }
                return false;
            }
        });
    }
	
	public static boolean updateIsPush(final String url) {
        return execute(new TransactionalOperation<Boolean>() {
            @Override
            public Boolean doInConnection(Connection connection) {
                try {
                	PreparedStatement saveStatement = connection.prepareStatement("update html_page set is_push=1 where url=?");
                	saveStatement.setString(1, url);
                    int result = saveStatement.executeUpdate();
                    return result > 0;
                } catch (SQLException e) {
                    error("update html_page failed ..." , e);
                }
                return false;
            }
        });
    }
	
	public static String findPushUrl() {
		return execute(new Operation<String>() {
            @Override
            public String doInConnection(Connection connection) {
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select url from html_page where is_push=0 limit 0,1");
                    if (resultSet.next()) {
                    	return resultSet.getString("url");
                    }
                } catch (SQLException e) {
                    error("query html_page failed ..." , e);
                }
                return null;
            }
        });
	}
	
}
