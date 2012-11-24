package net.simpleframework.module.druid.web;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

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
public class SqlMonitorPage extends AbstractMonitorPage {

	private static final String DATE_FORMAT = "MM-dd HH:mm:ss";

	@Override
	protected void onInit(final PageParameter pParameter) {
		super.onInit(pParameter);

		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pParameter,
				"SqlMonitorTable", TablePagerBean.class).setShowVerticalLine(true)
				.setDetailField("sqlDetail").setHeadHeight(54).setShowCheckbox(false)
				.setShowLineNo(true).setShowBorder(true).setPagerBarLayout(EPagerBarLayout.top)
				.setContainerId("idSqlMonitorTable").setHandleClass(SqlMonitorTable.class);

		tablePager
				.addColumn(new TablePagerColumn("ExecuteCount", "执行次数", 70))
				.addColumn(new TablePagerColumn("FetchRowCount", "读取行数", 70))
				.addColumn(new TablePagerColumn("EffectedRowCount", "影响行数", 70))
				.addColumn(new TablePagerColumn("RunningCount", "正在执\n行次数", 70))
				.addColumn(new TablePagerColumn("TotalTime", "总共时间", 70))
				.addColumn(new TablePagerColumn("MaxTimespan", "最慢消\n耗时间", 70).setFormat("#ms"))
				.addColumn(
						new TablePagerColumn("MaxTimespanOccurTime", "最慢发生时间", 110)
								.setFormat(DATE_FORMAT))
				.addColumn(new TablePagerColumn("ConcurrentMax", "最大并\n发数量 ", 70))
				.addColumn(new TablePagerColumn("ErrorCount", "错误次数", 70))
				.addColumn(new TablePagerColumn("BatchSizeMax", "最大\nBatch", 70))
				.addColumn(new TablePagerColumn("BatchSizeTotal", "所有\nBatch", 70))
				.addColumn(
						new TablePagerColumn("ResultSetHoldTime", "ResultSet\n持有时间", 100)
								.setFormat("#ms"))
				.addColumn(
						new TablePagerColumn("ExecuteAndResultSetHoldTime", "ResultSet\n执行及持有时间", 110)
								.setFormat("#ms"))
				.addColumn(new TablePagerColumn("InTransactionCount", "事务中\n运行数", 70))
				.addColumn(new TablePagerColumn("LastTime", "最后执行时间", 110).setFormat(DATE_FORMAT))
				.addColumn(new TablePagerColumn("LastErrorTime", "最后错误时间", 110).setFormat(DATE_FORMAT))
				.addColumn(TablePagerColumn.BLANK);

		for (final TablePagerColumn c : tablePager.getColumns().values()) {
			c.setTooltip(c.getColumnName());
		}
	}

	public static class SqlMonitorTable extends AbstractTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cParameter) {
			TabularData sqls;
			try {
				sqls = JdbcStatManager.getInstance().getSqlList();
			} catch (final JMException e) {
				throw ComponentHandleException.of(e);
			}

			// EffectedRowCountHistogram, ,
			// ExecuteAndResultHoldTimeHistogram, ,
			// FetchRowCountHistogram, ,
			// Histogram, ID
			// LastSlowParameters,
			final ArrayList<Map<?, ?>> data = new ArrayList<Map<?, ?>>();
			final Iterator<?> it = sqls.values().iterator();
			while (it.hasNext()) {
				final CompositeData val = (CompositeData) it.next();
				final Map<String, Object> row = new HashMap<String, Object>();

				for (final String k : new String[] { "SQL", "ExecuteCount", "FetchRowCount",
						"EffectedRowCount", "RunningCount", "TotalTime", "ErrorCount", "ConcurrentMax",
						"MaxTimespan", "MaxTimespanOccurTime", "BatchSizeMax", "BatchSizeTotal",
						"ResultSetHoldTime", "ExecuteAndResultSetHoldTime", "InTransactionCount",
						"LastTime", "LastError", "LastErrorTime" }) {
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

					final double l = ((Long) BeanUtils.getProperty(dataObject, "TotalTime"))
							.doubleValue() / 1000.0;
					kv.put("TotalTime", NumberUtils.formatDouble(l) + "s");

					final StringBuilder sb = new StringBuilder();
					final String sql = (String) BeanUtils.getProperty(dataObject, "SQL");
					if (StringUtils.hasText(sql)) {
						sb.append("<tr><td class='l'>SQL</td><td class='v'>");
						sb.append(sql).append("</td></tr>");
					}
					final CompositeData lastError = (CompositeData) kv.get("LastError");
					if (lastError != null) {
						sb.append("<tr><td class='l'>").append("最后一次错误栈");
						sb.append("</td><td class='v'>").append(lastError.get("stackTrace"));
						sb.append("</td></tr>");
					}
					if (sb.length() > 0) {
						sb.insert(0, "<table class='form_tbl'>");
						sb.append("</table>");
						kv.put("sqlDetail", sb.toString());
					}

					return kv;
				}
			};
		}
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pParameter) {
		return super.getNavigationBar(pParameter)
				.append(new LinkElement($m("AbstractMonitorPage.2")));
	}
}
