package net.simpleframework.module.druid.web;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import net.simpleframework.common.I18n;
import net.simpleframework.common.NumberUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.ado.query.IDataQuery;
import net.simpleframework.common.ado.query.ListDataObjectQuery;
import net.simpleframework.common.bean.BeanUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.html.element.LinkElement;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentHandleException;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerHandler;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.template.struct.NavigationButtons;

import com.alibaba.druid.stat.JdbcStatManager;

/**
 * 这是一个开源的软件，请在LGPLv3下合法使用、修改或重新发布。
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class ConnectionMonitorPage extends AbstractMonitorPage {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	protected void onInit(final PageParameter pParameter) {
		super.onInit(pParameter);

		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pParameter,
				"DataSourceMonitorTable", TablePagerBean.class).setShowVerticalLine(true)
				.setScrollHead(false).setDetailField("connDetail").setShowCheckbox(false)
				.setShowLineNo(true).setPageItems(999).setPagerBarLayout(EPagerBarLayout.none)
				.setContainerId("idConnectionMonitorTable")
				.setHandleClass(ConnectionMonitorTable.class);

		tablePager
				.addColumn(new TablePagerColumn("ID").setWidth(80))
				.addColumn(
						new TablePagerColumn("ConnectTime", $m("ConnectionMonitorPage.ConnectTime"))
								.setFormat(DATE_FORMAT))
				.addColumn(
						new TablePagerColumn("ConnectTimespan", I18n
								.$m("ConnectionMonitorPage.ConnectTimespan")))
				.addColumn(
						new TablePagerColumn("EstablishTime", I18n
								.$m("ConnectionMonitorPage.EstablishTime")).setFormat(DATE_FORMAT))
				.addColumn(
						new TablePagerColumn("AliveTimespan", I18n
								.$m("ConnectionMonitorPage.AliveTimespan")))
				.addColumn(
						new TablePagerColumn("LastErrorTime", I18n
								.$m("ConnectionMonitorPage.LastErrorTime")).setFormat(DATE_FORMAT));
	}

	public static class ConnectionMonitorTable extends AbstractTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cParameter) {
			TabularData coll;
			try {
				coll = JdbcStatManager.getInstance().getConnectionList();
			} catch (final JMException e) {
				throw ComponentHandleException.of(e);
			}

			final ArrayList<Map<?, ?>> data = new ArrayList<Map<?, ?>>();
			final Iterator<?> it = coll.values().iterator();
			while (it.hasNext()) {
				final Map<String, Object> row = new HashMap<String, Object>();
				final CompositeData val = (CompositeData) it.next();

				for (final String k : new String[] { "AliveTimespan", "ID", "ConnectStatckTrace",
						"ConnectTime", "ConnectTimespan", "EstablishTime", "LastError", "LastErrorTime",
						"LastSql", "LastStatementStackTrace" }) {
					row.put(k, val.get(k));
				}
				data.add(row);
			}
			return new ListDataObjectQuery<Map<?, ?>>(data);
		}

		@Override
		public AbstractTablePagerSchema createTablePagerSchema() {
			return new DefaultTablePagerSchema() {
				@Override
				public Map<String, Object> getRowData(final ComponentParameter cParameter,
						final Object dataObject) {
					final KVMap kv = (KVMap) super.getRowData(cParameter, dataObject);

					double l = ((Long) BeanUtils.getProperty(dataObject, "AliveTimespan")).doubleValue() / 1000.0;
					String ts = NumberUtils.formatDouble(l) + "s";
					if (l > 60) {
						ts += "&nbsp;(" + NumberUtils.formatDouble(l / 60.0) + "m)";
					}
					kv.put("AliveTimespan", ts);

					l = ((Long) BeanUtils.getProperty(dataObject, "ConnectTimespan")).doubleValue() / 1000.0;
					kv.put("ConnectTimespan", NumberUtils.formatDouble(l) + "s");

					final StringBuilder sb = new StringBuilder();
					final String lastSql = (String) BeanUtils.getProperty(dataObject, "LastSql");
					if (StringUtils.hasText(lastSql)) {
						sb.append("<tr><td class='l'>").append($m("ConnectionMonitorPage.0"));
						sb.append("</td><td class='v'>");
						sb.append(lastSql).append("</td></tr>");
					}
					final CompositeData lastError = ((CompositeData) BeanUtils.getProperty(dataObject,
							"LastError"));
					if (lastError != null) {
						sb.append("<tr><td class='l'>").append($m("ConnectionMonitorPage.1"));
						sb.append("</td><td class='v'>").append(lastError.get("stackTrace"));
						sb.append("</td></tr>");
					}
					final Object lastStatementStackTrace = BeanUtils.getProperty(dataObject,
							"LastStatementStackTrace");
					if (lastStatementStackTrace != null) {
						sb.append("<tr><td class='l'>").append($m("ConnectionMonitorPage.2"));
						sb.append("</td><td class='v'>").append(lastStatementStackTrace);
						sb.append("</td></tr>");
					}
					if (sb.length() > 0) {
						sb.insert(0, "<table class='form_tbl'>");
						sb.append("</table>");
						kv.put("connDetail", sb.toString());
					}
					return kv;
				}
			};
		}
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pParameter) {
		return super.getNavigationBar(pParameter)
				.append(new LinkElement($m("AbstractMonitorPage.1")));
	}
}
