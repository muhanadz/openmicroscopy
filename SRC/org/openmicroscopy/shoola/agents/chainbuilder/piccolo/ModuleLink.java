/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleLink
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
 package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;
 
 //Java imports
import java.awt.geom.Point2D;

//Third-party libraries
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.piccolo.PConstants;
 
/** 
 * A Piccolo node for links between between {@link ModuleView} objects. These 
 * links are shown when the magnification level is too low to show the 
 * individual links between parameters. Thus, this class of link is used for
 * semantic zooming.
 *
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */ 
 public class ModuleLink extends Link {
 	
 	/**
 	 * The {@link ModuleView}s that are the start and end of this link. Unlike
 	 * {@link Link}, objects of this class always have start being the output
 	 * and end being the input
 	 */
 	private ModuleView start;
 	
 	private ModuleView end;
 	
 
 	/**
 	 * Create the node in the appropriate layer, and establish listeners:
 	 * this link wants to hear from events at either end
 	 * @param layer
 	 * @param start
 	 * @param end
 	 */
 	public ModuleLink(LinkLayer layer,ModuleView start,ModuleView end) {
 		super();
 		setLinkLayer(layer);
 		this.start= start;
 		this.end = end;
 		
 		start.addNodeEventListener(this);
 		end.addNodeEventListener(this);
 		setStartPoint();
 		setEndPoint();
 	}
 	
 	public ModuleLink(LinkLayer layer,Link link,ModuleView start,ModuleView end) {
 		this(layer,start,end);
 		// copy points 1 to n-2 (point 0 comes from start point,
 		// point n-1 comes from end point.
 		
 		int n = link.pointCount();
 		for (int i=1; i <n-2; i++) {
 			Point2D point = link.getPoint(i);
 			insertIntermediatePoint(i,(float) point.getX(),
 				(float) point.getY());
 		}
 	}
 	
 	public LinkTarget getStartLinkTarget() {
 		return start.getOutputLinkTarget();
 	}
 	
 	public LinkTarget getEndLinkTarget() {
 		return end.getInputLinkTarget();
 	}
 	
 	private void setStartPoint() {
 		Point2D point = getStartLinkTarget().getCenter();
 		globalToLocal(point);
 		
 		setStartCoords((float) point.getX(),(float) point.getY()); 	
 	}
 	
	private void setEndPoint() {
		Point2D point = getEndLinkTarget().getCenter();
		globalToLocal(point);
		
		setEndCoords((float) point.getX()-PConstants.LINK_BULB_RADIUS,
			(float) point.getY());
	}
	
	/**
	 * When this object gets a node changed event, call {@link setStartPoint()}
	 * or {@link setEndPoint}. The calls will look at the location of the 
	 * appropriate {@link LinkTarget}. Since the {@ModuleView} has changed,
	 * the {@link LinkTarget} will have changed, and therefore the 
	 * points will be updated as needed.
	 */
	public void nodeChanged(NodeEvent e) {
		PNode node = e.getNode();
		if (!(node instanceof ModuleView))
			return;
		if (node == start) {
			setStartPoint();
			setLine();
		}
		else if (node == end)
			setEndPoint();
	}
	
	
	
	/**
	 * Draw the end of the link at the appropriate point. Since the end of the 
	 * link is always the side that goes to an input, we just draw the link 
	 * end at the last point in the list. 
	 */	
	protected void setLine() {
			
		int n = points.size();
		Point2D end = (Point2D) points.get(n-1);
		drawLinkEnd((float) end.getX(),(float)end.getY());
	}
	
	protected ModuleView getStart() {
		return start;
	}
	
	protected ModuleView getEnd() { 
		return end;
	}
	
	/**
	 * Remove this link from the targets and from the {@link LinkLayer}
	 */
	public void remove() {
		super.remove();
		getStartLinkTarget().setSelected(false);
		getEndLinkTarget().setSelected(false);
		if (linkLayer!= null)
			linkLayer.removeParamLinks(this);
	}
 }