package com.floreantpos.jreports;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.print.JRPrinterAWT;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.floreantpos.main.Application;
import com.floreantpos.model.Restaurant;
import com.floreantpos.model.Terminal;
import com.floreantpos.model.Ticket;
import com.floreantpos.model.TicketItem;
import com.floreantpos.model.TicketItemModifier;
import com.floreantpos.model.TicketItemModifierGroup;
import com.floreantpos.model.DrawerPullReport;
import com.floreantpos.model.dao.RestaurantDAO;

public class JReportPrintService {
	private static Log logger = LogFactory.getLog(JReportPrintService.class);

    public static void printTicket(Ticket ticket) {
        Restaurant restaurant = RestaurantDAO.getInstance().get(Integer.valueOf(1));

        HashMap map = new HashMap();
        map.put("restaurantName", restaurant.getName());
        map.put("addressLine1",   restaurant.getAddressLine1());
        map.put("addressLine2",   restaurant.getAddressLine2());
        map.put("addressLine3",   restaurant.getAddressLine3());
        map.put("telephone",      com.floreantpos.POSConstants.TEL + ": " + restaurant.getTelephone());

        map.put("checkNo",       com.floreantpos.POSConstants.CHK_NO + ticket.getId());
        map.put("tableNo",       com.floreantpos.POSConstants.TABLE_NO + ticket.getTableNumber());
        map.put("guestCount",    com.floreantpos.POSConstants.GUESTS_ + ticket.getNumberOfGuests());
        map.put("serverName",    com.floreantpos.POSConstants.SERVER + ": " + ticket.getOwner());
        map.put("reportDate",    com.floreantpos.POSConstants.DATE + ": " + Application.formatDate(new Date()));
        map.put("grandSubtotal", Application.formatNumber(ticket.getSubtotalAmount()));
        map.put("grandTotal",    Application.formatNumber(ticket.getTotalAmount()));
        map.put("taxAmount",     Application.formatNumber(ticket.getTaxAmount()));
        if (ticket.getGratuity() != null) {
            map.put("tipAmount", Application.formatNumber(ticket.getGratuity().getAmount()));
        }

        InputStream ticketReportStream = null;

        try {
            ticketReportStream = JReportPrintService.class.getResourceAsStream("/com/floreantpos/jreports/TicketReceiptReport.jasper");

            JasperReport ticketReport = (JasperReport) JRLoader.loadObject(ticketReportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(ticketReport, map, new JRTableModelDataSource(new TicketDataSource(ticket)));
            JasperPrintManager.printReport(jasperPrint, false);

        } catch (JRException e) {
            logger.error(com.floreantpos.POSConstants.PRINT_ERROR, e);

        } finally {
            try {
                ticketReportStream.close();
            } catch (Exception x) {
            }
        }
}

    public static void printTicketToKitchen(Ticket ticket) {
        Restaurant restaurant = RestaurantDAO.getInstance().get(Integer.valueOf(1));

        HashMap map = new HashMap();

        map.put("restaurantName", restaurant.getName());
        map.put("checkNo",        com.floreantpos.POSConstants.CHK_NO + ticket.getId());
        map.put("tableNo",        com.floreantpos.POSConstants.TABLE_NO + ticket.getTableNumber());
        map.put("guestCount",     com.floreantpos.POSConstants.GUESTS_ + ticket.getNumberOfGuests());
        map.put("serverName",     com.floreantpos.POSConstants.SERVER + ": " + ticket.getOwner());
        map.put("reportDate",     com.floreantpos.POSConstants.DATE + ": " + Application.formatDate(new Date()));

        InputStream ticketReportStream = null;

        try {
            ticketReportStream = JReportPrintService.class.getResourceAsStream("/com/floreantpos/jreports/KitchenReceipt.jasper");
            JasperReport ticketReport = (JasperReport) JRLoader.loadObject(ticketReportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(ticketReport, map, new JRTableModelDataSource(new KitchenTicketDataSource(ticket)));

            JRPrinterAWT.printToKitchen = true;
            JasperPrintManager.printReport(jasperPrint, false);

            //no exception, so print to kitchen successful.
            //now mark items as printed.
            markItemsAsPrinted(ticket);

        } catch (JRException e) {
            logger.error(com.floreantpos.POSConstants.PRINT_ERROR, e);

        } finally {
            try {
                ticketReportStream.close();
            } catch (Exception x) {
            }
        }
    }

    public static void printTicketToBar(Ticket ticket) {
        Restaurant restaurant = RestaurantDAO.getInstance().get(Integer.valueOf(1));

        HashMap map = new HashMap();

        map.put("restaurantName", restaurant.getName());
        map.put("checkNo",        com.floreantpos.POSConstants.CHK_NO   + ticket.getId());
        map.put("tableNo",        com.floreantpos.POSConstants.TABLE_NO + ticket.getTableNumber());
        map.put("guestCount",     com.floreantpos.POSConstants.GUESTS_  + ticket.getNumberOfGuests());
        map.put("serverName",     com.floreantpos.POSConstants.SERVER   + ": " + ticket.getOwner());
        map.put("reportDate",     com.floreantpos.POSConstants.DATE     + ": " + Application.formatDate(new Date()));

        InputStream ticketReportStream = null;

        try {
            ticketReportStream = JReportPrintService.class.getResourceAsStream("/com/floreantpos/jreports/BarReceipt.jasper");
            JasperReport ticketReport = (JasperReport) JRLoader.loadObject(ticketReportStream);
            JasperPrint jasperPrint   = JasperFillManager.fillReport(ticketReport, map, new JRTableModelDataSource(new BarTicketDataSource(ticket)));

            JRPrinterAWT.printToBar = true;
            JasperPrintManager.printReport(jasperPrint, false);

            markItemsAsPrinted(ticket);

        } catch (JRException e) {
            logger.error(com.floreantpos.POSConstants.PRINT_ERROR, e);

        } finally {
            try {
                ticketReportStream.close();
            } catch (Exception x) {
            }
        }
    }

	private static void markItemsAsPrinted(Ticket ticket) {
		List<TicketItem> ticketItems = ticket.getTicketItems();
		if (ticketItems != null) {
			for (TicketItem ticketItem : ticketItems) {
				if (!ticketItem.isPrintedToKitchen()) {
					ticketItem.setPrintedToKitchen(true);
				}

				List<TicketItemModifierGroup> modifierGroups = ticketItem.getTicketItemModifierGroups();
				if (modifierGroups != null) {
					for (TicketItemModifierGroup modifierGroup : modifierGroups) {
						List<TicketItemModifier> modifiers = modifierGroup.getTicketItemModifiers();
						if (modifiers != null) {
							for (TicketItemModifier modifier : modifiers) {
								if (!modifier.isPrintedToKitchen()) {
									modifier.setPrintedToKitchen(true);
								}
							}
						}
					}
				}

			}
		}
	}

    public static void printDrawerPullReport(DrawerPullReport drawerPullReport, Terminal terminal) {

        Restaurant restaurant = RestaurantDAO.getInstance().get(Integer.valueOf(1));

        HashMap map = new HashMap();

        map.put("restaurantName",           restaurant.getName());
        map.put("reportDate",               Application.formatDate(new Date()));
        map.put("cashReceiptNumber",        drawerPullReport.getCashReceiptNumber().toString());
        map.put("netSales",                 Application.formatNumber(drawerPullReport.getNetSales()));
        map.put("salesTax",                 Application.formatNumber(drawerPullReport.getSalesTax()));
        map.put("totalRevenue",             Application.formatNumber(drawerPullReport.getTotalRevenue()));
        map.put("chargedTips",              Application.formatNumber(drawerPullReport.getChargedTips()));
        map.put("grossReceipts",            Application.formatNumber(drawerPullReport.getGrossReceipts()));
        map.put("getCashReceiptNumber",     drawerPullReport.getCashReceiptNumber().toString());
        map.put("cashReceiptAmount",        Application.formatNumber(drawerPullReport.getCashReceiptAmount()));
        map.put("creditCardReceiptNumber",  drawerPullReport.getCreditCardReceiptNumber().toString());
        map.put("creditCardReceiptAmount",  Application.formatNumber(drawerPullReport.getCreditCardReceiptAmount()));
        map.put("debitCardReceiptNumber",   drawerPullReport.getDebitCardReceiptNumber().toString());
        map.put("debitCardReceiptAmount",   Application.formatNumber(drawerPullReport.getDebitCardReceiptAmount()));
        map.put("giftCertReturnNumber",     drawerPullReport.getGiftCertReturnCount().toString());
        map.put("giftCertReturnAmount",     Application.formatNumber(drawerPullReport.getGiftCertReturnAmount()));
        map.put("giftCertChangeAmount",     Application.formatNumber(drawerPullReport.getGiftCertChangeAmount()));
        map.put("cashBack",                 Application.formatNumber(drawerPullReport.getCashBack()));
        map.put("receiptDifferential",      Application.formatNumber(drawerPullReport.getReceiptDifferential()));
        map.put("chargedTips",              Application.formatNumber(drawerPullReport.getChargedTips()));
        map.put("tipsPaid",                 Application.formatNumber(drawerPullReport.getTipsPaid()));
        map.put("tipsDifferential",         Application.formatNumber(drawerPullReport.getTipsDifferential()));
        map.put("cashReceiptNumber",        drawerPullReport.getCashReceiptNumber().toString());
        map.put("cashReceiptAmount",        Application.formatNumber(drawerPullReport.getCashReceiptAmount()));
        map.put("tipsPaid",                 Application.formatNumber(drawerPullReport.getTipsPaid()));
        map.put("payOutNumber",             drawerPullReport.getPayOutNumber().toString());
        map.put("payOutAmount",             Application.formatNumber(drawerPullReport.getPayOutAmount()));
        map.put("cashBack",                 Application.formatNumber(drawerPullReport.getCashBack()));
        map.put("openingBalance",           Application.formatNumber(terminal.getOpeningBalance()));
        map.put("drawerBleedNumber",        drawerPullReport.getDrawerBleedNumber().toString());
        map.put("drawerBleedAmount",        Application.formatNumber(drawerPullReport.getDrawerBleedAmount()));
        map.put("drawerAccountable",        Application.formatNumber(drawerPullReport.getDrawerAccountable()));
        map.put("currentBalance",           Application.formatNumber(terminal.getCurrentBalance()));

        InputStream drawerReportStream = null;

        try {
            drawerReportStream = JReportPrintService.class.getResourceAsStream("/com/floreantpos/jreports/DrawerPullReport.jasper");

            JasperReport drawerReport = (JasperReport) JRLoader.loadObject(drawerReportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(drawerReport, map);
            JasperPrintManager.printReport(jasperPrint, false);

        } catch (JRException e) {
            logger.error(com.floreantpos.POSConstants.PRINT_ERROR, e);

        } finally {
            try {
                drawerReportStream.close();
            } catch (Exception x) {
            }
        }
    }
}
