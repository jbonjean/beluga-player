/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.skin.MenuButtonSkin;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

/**
 * Terra menu button skin.
 */
public class BelugaMenuButtonSkin extends MenuButtonSkin
{
	private Font font;
	private Color color;
	private Color disabledColor;
	private Color backgroundColor;
	private Color disabledBackgroundColor;
	private Color borderColor;
	private Color disabledBorderColor;
	private Insets padding;
	private int spacing;
	private float minumumAspectRatio;
	private float maximumAspectRatio;
	private boolean toolbar;

	private WindowStateListener menuPopupWindowStateListener = new WindowStateListener.Adapter()
	{
		@Override
		public void windowOpened(Window window)
		{
			MenuButton menuButton = (MenuButton) getComponent();

			// Size and position the popup
			Display display = menuButton.getDisplay();
			Dimensions displaySize = display.getSize();

			Point buttonLocation = menuButton.mapPointToAncestor(display, 0, 0);
			window.setLocation(buttonLocation.x, buttonLocation.y + getHeight() - 1);

			int width = getWidth();
			window.setMinimumWidth(width - TRIGGER_WIDTH - 1);

			// If the popup extends over the right edge of the display,
			// move it so that the right edge of the popup lines up with the
			// right edge of the button
			int popupWidth = window.getPreferredWidth();
			if (buttonLocation.x + popupWidth > displaySize.width)
			{
				window.setX(buttonLocation.x + width - popupWidth);
			}

			window.setMaximumHeight(Integer.MAX_VALUE);
			int popupHeight = window.getPreferredHeight();
			int maximumHeight = displaySize.height - window.getY();
			if (popupHeight > maximumHeight && buttonLocation.y > maximumHeight)
			{
				window.setMaximumHeight(buttonLocation.y);
				window.setY(buttonLocation.y - window.getPreferredHeight() + 1);
			}
			else
			{
				window.setMaximumHeight(maximumHeight);
			}

			repaintComponent();
		}

		@Override
		public void windowClosed(Window window, Display display, Window owner)
		{
			repaintComponent();
		}
	};

	private static final int TRIGGER_WIDTH = 10;

	public BelugaMenuButtonSkin()
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();

		font = theme.getFont();
		color = theme.getColor(1);
		disabledColor = theme.getColor(7);
		backgroundColor = theme.getColor(10);
		disabledBackgroundColor = theme.getColor(10);
		borderColor = theme.getColor(7);
		disabledBorderColor = theme.getColor(7);
		padding = new Insets(3);
		spacing = 0;
		minumumAspectRatio = Float.NaN;
		maximumAspectRatio = Float.NaN;
		toolbar = true;

		menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
	}

	@Override
	public int getPreferredWidth(int height)
	{
		int preferredWidth = 0;

		if (height == -1)
		{
			preferredWidth = getPreferredSize().width;
		}
		else
		{
			MenuButton menuButton = (MenuButton) getComponent();
			Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
			dataRenderer.render(menuButton.getButtonData(), menuButton, false);

			preferredWidth = dataRenderer.getPreferredWidth(-1) + TRIGGER_WIDTH + padding.left + padding.right + spacing + 2;

			// Adjust for preferred aspect ratio
			if (!Float.isNaN(minumumAspectRatio) && (float) preferredWidth / (float) height < minumumAspectRatio)
			{
				preferredWidth = (int) (height * minumumAspectRatio);
			}
		}

		return preferredWidth;
	}

	@Override
	public int getPreferredHeight(int width)
	{
		int preferredHeight = 0;

		if (width == -1)
		{
			preferredHeight = getPreferredSize().height;
		}
		else
		{
			MenuButton menuButton = (MenuButton) getComponent();

			Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
			dataRenderer.render(menuButton.getButtonData(), menuButton, false);

			preferredHeight = dataRenderer.getPreferredHeight(-1) + padding.top + padding.bottom + 2;

			// Adjust for preferred aspect ratio
			if (!Float.isNaN(maximumAspectRatio) && (float) width / (float) preferredHeight > maximumAspectRatio)
			{
				preferredHeight = (int) (width / maximumAspectRatio);
			}
		}

		return preferredHeight;
	}

	@Override
	public Dimensions getPreferredSize()
	{
		MenuButton menuButton = (MenuButton) getComponent();

		Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
		dataRenderer.render(menuButton.getButtonData(), menuButton, false);

		Dimensions contentSize = dataRenderer.getPreferredSize();
		int preferredWidth = contentSize.width + TRIGGER_WIDTH + padding.left + padding.right + 2;
		int preferredHeight = contentSize.height + padding.top + padding.bottom + 2;

		// Adjust for preferred aspect ratio
		float aspectRatio = (float) preferredWidth / (float) preferredHeight;

		if (!Float.isNaN(minumumAspectRatio) && aspectRatio < minumumAspectRatio)
		{
			preferredWidth = (int) (preferredHeight * minumumAspectRatio);
		}

		if (!Float.isNaN(maximumAspectRatio) && aspectRatio > maximumAspectRatio)
		{
			preferredHeight = (int) (preferredWidth / maximumAspectRatio);
		}

		return new Dimensions(preferredWidth, preferredHeight);
	}

	@Override
	public int getBaseline(int width, int height)
	{
		MenuButton menuButton = (MenuButton) getComponent();

		Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
		dataRenderer.render(menuButton.getButtonData(), menuButton, false);

		int clientWidth = Math.max(width - (TRIGGER_WIDTH + padding.left + padding.right + 2), 0);
		int clientHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

		int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);

		if (baseline != -1)
		{
			baseline += padding.top + 1;
		}

		return baseline;
	}

	@Override
	public void layout()
	{
		// No-op
	}

	@Override
	public void paint(Graphics2D graphics)
	{
		MenuButton menuButton = (MenuButton) getComponent();

		int width = getWidth();
		int height = getHeight();

		// Paint the content
		Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
		dataRenderer.render(menuButton.getButtonData(), menuButton, false);
		dataRenderer.setSize(width, height);

		dataRenderer.paint(graphics);
	}

	@Override
	public boolean isFocusable()
	{
		return false;
	}

	@Override
	public boolean isOpaque()
	{
		MenuButton menuButton = (MenuButton) getComponent();
		return (!toolbar || highlighted || menuButton.isFocused());
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		if (font == null)
		{
			throw new IllegalArgumentException("font is null.");
		}

		this.font = font;
		invalidateComponent();
	}

	public final void setFont(String font)
	{
		if (font == null)
		{
			throw new IllegalArgumentException("font is null.");
		}

		setFont(decodeFont(font));
	}

	public final void setFont(Dictionary<String, ?> font)
	{
		if (font == null)
		{
			throw new IllegalArgumentException("font is null.");
		}

		setFont(Theme.deriveFont(font));
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		if (color == null)
		{
			throw new IllegalArgumentException("color is null.");
		}

		this.color = color;
		repaintComponent();
	}

	public final void setColor(String color)
	{
		if (color == null)
		{
			throw new IllegalArgumentException("color is null.");
		}

		setColor(GraphicsUtilities.decodeColor(color));
	}

	public final void setColor(int color)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setColor(theme.getColor(color));
	}

	public Color getDisabledColor()
	{
		return disabledColor;
	}

	public void setDisabledColor(Color disabledColor)
	{
		if (disabledColor == null)
		{
			throw new IllegalArgumentException("disabledColor is null.");
		}

		this.disabledColor = disabledColor;
		repaintComponent();
	}

	public final void setDisabledColor(String disabledColor)
	{
		if (disabledColor == null)
		{
			throw new IllegalArgumentException("disabledColor is null.");
		}

		setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
	}

	public final void setDisabledColor(int disabledColor)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setDisabledColor(theme.getColor(disabledColor));
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		if (backgroundColor == null)
		{
			throw new IllegalArgumentException("backgroundColor is null.");
		}

		this.backgroundColor = backgroundColor;
		repaintComponent();
	}

	public final void setBackgroundColor(String backgroundColor)
	{
		if (backgroundColor == null)
		{
			throw new IllegalArgumentException("backgroundColor is null.");
		}

		setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
	}

	public final void setBackgroundColor(int backgroundColor)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setBackgroundColor(theme.getColor(backgroundColor));
	}

	public Color getDisabledBackgroundColor()
	{
		return disabledBackgroundColor;
	}

	public void setDisabledBackgroundColor(Color disabledBackgroundColor)
	{
		if (disabledBackgroundColor == null)
		{
			throw new IllegalArgumentException("disabledBackgroundColor is null.");
		}

		this.disabledBackgroundColor = disabledBackgroundColor;
		repaintComponent();
	}

	public final void setDisabledBackgroundColor(String disabledBackgroundColor)
	{
		if (disabledBackgroundColor == null)
		{
			throw new IllegalArgumentException("disabledBackgroundColor is null.");
		}

		setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
	}

	public final void setDisabledBackgroundColor(int disabledBackgroundColor)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setDisabledBackgroundColor(theme.getColor(disabledBackgroundColor));
	}

	public Color getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		if (borderColor == null)
		{
			throw new IllegalArgumentException("borderColor is null.");
		}

		this.borderColor = borderColor;
		menuPopup.getStyles().put("borderColor", borderColor);
		repaintComponent();
	}

	public final void setBorderColor(String borderColor)
	{
		if (borderColor == null)
		{
			throw new IllegalArgumentException("borderColor is null.");
		}

		setBorderColor(GraphicsUtilities.decodeColor(borderColor));
	}

	public final void setBorderColor(int borderColor)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setBorderColor(theme.getColor(borderColor));
	}

	public Color getDisabledBorderColor()
	{
		return disabledBorderColor;
	}

	public void setDisabledBorderColor(Color disabledBorderColor)
	{
		if (disabledBorderColor == null)
		{
			throw new IllegalArgumentException("disabledBorderColor is null.");
		}

		this.disabledBorderColor = disabledBorderColor;
		repaintComponent();
	}

	public final void setDisabledBorderColor(String disabledBorderColor)
	{
		if (disabledBorderColor == null)
		{
			throw new IllegalArgumentException("disabledBorderColor is null.");
		}

		setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
	}

	public final void setDisabledBorderColor(int disabledBorderColor)
	{
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		setDisabledBorderColor(theme.getColor(disabledBorderColor));
	}

	public Insets getPadding()
	{
		return padding;
	}

	public void setPadding(Insets padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}

		this.padding = padding;
		invalidateComponent();
	}

	public final void setPadding(Dictionary<String, ?> padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}

		setPadding(new Insets(padding));
	}

	public final void setPadding(int padding)
	{
		setPadding(new Insets(padding));
	}

	public final void setPadding(Number padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}

		setPadding(padding.intValue());
	}

	public final void setPadding(String padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}

		setPadding(Insets.decode(padding));
	}

	public int getSpacing()
	{
		return spacing;
	}

	public void setSpacing(int spacing)
	{
		if (spacing < 0)
		{
			throw new IllegalArgumentException("spacing is negative.");
		}
		this.spacing = spacing;
		invalidateComponent();
	}

	public final void setSpacing(Number spacing)
	{
		if (spacing == null)
		{
			throw new IllegalArgumentException("spacing is null.");
		}

		setSpacing(spacing.intValue());
	}

	public float getMinimumAspectRatio()
	{
		return minumumAspectRatio;
	}

	public void setMinimumAspectRatio(float minumumAspectRatio)
	{
		if (!Float.isNaN(maximumAspectRatio) && minumumAspectRatio > maximumAspectRatio)
		{
			throw new IllegalArgumentException("minumumAspectRatio is greater than maximumAspectRatio.");
		}

		this.minumumAspectRatio = minumumAspectRatio;
		invalidateComponent();
	}

	public final void setMinimumAspectRatio(Number minumumAspectRatio)
	{
		if (minumumAspectRatio == null)
		{
			throw new IllegalArgumentException("minumumAspectRatio is null.");
		}

		setMinimumAspectRatio(minumumAspectRatio.floatValue());
	}

	public float getMaximumAspectRatio()
	{
		return maximumAspectRatio;
	}

	public void setMaximumAspectRatio(float maximumAspectRatio)
	{
		if (!Float.isNaN(minumumAspectRatio) && maximumAspectRatio < minumumAspectRatio)
		{
			throw new IllegalArgumentException("maximumAspectRatio is less than minimumAspectRatio.");
		}

		this.maximumAspectRatio = maximumAspectRatio;
		invalidateComponent();
	}

	public final void setMaximumAspectRatio(Number maximumAspectRatio)
	{
		if (maximumAspectRatio == null)
		{
			throw new IllegalArgumentException("maximumAspectRatio is null.");
		}

		setMaximumAspectRatio(maximumAspectRatio.floatValue());
	}

	public boolean isToolbar()
	{
		return toolbar;
	}

	public void setToolbar(boolean toolbar)
	{
		this.toolbar = toolbar;

		if (toolbar && getComponent().isFocused())
		{
			Component.clearFocus();
		}

		repaintComponent();
	}

	public int getCloseTransitionDuration()
	{
		return (Integer) menuPopup.getStyles().get("closeTransitionDuration");
	}

	public void setCloseTransitionDuration(int closeTransitionDuration)
	{
		menuPopup.getStyles().put("closeTransitionDuration", closeTransitionDuration);
	}

	public int getCloseTransitionRate()
	{
		return (Integer) menuPopup.getStyles().get("closeTransitionRate");
	}

	public void setCloseTransitionRate(int closeTransitionRate)
	{
		menuPopup.getStyles().put("closeTransitionRate", closeTransitionRate);
	}

	@Override
	public void mouseOut(Component component)
	{
		super.mouseOut(component);

		if (toolbar && component.isFocused())
		{
			Component.clearFocus();
		}
	}

	@Override
	public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
	{
		if (!toolbar)
		{
			component.requestFocus();
		}

		return super.mouseClick(component, button, x, y, count);
	}
}
