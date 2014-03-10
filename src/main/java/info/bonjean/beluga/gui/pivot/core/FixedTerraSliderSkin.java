package info.bonjean.beluga.gui.pivot.core;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.terra.TerraSliderSkin;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Fixes the missing disabled state color changes.
 * This is really a dirty workaround but avoid copying too much code from the
 * original class.
 * 
 */
public class FixedTerraSliderSkin extends TerraSliderSkin
{
	private Color buttonBackgroundColor;
	private Color disabledButtonBackgroundColor;
	private Color buttonBorderColor;
	private Color disabledButtonBorderColor;
	private Color trackColor;
	private Color disabledTrackColor;

	public FixedTerraSliderSkin()
	{
		super();

		TerraTheme theme = (TerraTheme) Theme.getTheme();
		disabledButtonBackgroundColor = theme.getColor(6);
		disabledButtonBorderColor = theme.getColor(6);
		disabledTrackColor = theme.getColor(6);
	}

	@Override
	public void setTrackColor(Color trackColor)
	{
		if (this.trackColor == null)
			this.trackColor = trackColor;
		super.setTrackColor(trackColor);
	}

	@Override
	public void setButtonBackgroundColor(Color buttonBackgroundColor)
	{
		if (this.buttonBackgroundColor == null)
			this.buttonBackgroundColor = buttonBackgroundColor;
		super.setButtonBackgroundColor(buttonBackgroundColor);
	}

	@Override
	public void setButtonBorderColor(Color buttonBorderColor)
	{
		if (this.buttonBorderColor == null)
			this.buttonBorderColor = buttonBorderColor;
		super.setButtonBorderColor(buttonBorderColor);
	}

	@Override
	public void paint(Graphics2D graphics)
	{
		// avoid errors
		if (buttonBackgroundColor == null)
			buttonBackgroundColor = getButtonBackgroundColor();
		if (buttonBorderColor == null)
			buttonBorderColor = getButtonBorderColor();
		if (trackColor == null)
			trackColor = getTrackColor();

		Slider slider = (Slider) FixedTerraSliderSkin.this.getComponent();
		// override colors, this way we don't have to redefine the whole
		// paint method
		if (slider.isEnabled())
		{
			setButtonBackgroundColor(buttonBackgroundColor);
			setButtonBorderColor(buttonBorderColor);
			setTrackColor(trackColor);
		}
		else
		{
			setButtonBackgroundColor(disabledButtonBackgroundColor);
			setButtonBorderColor(disabledButtonBorderColor);
			setTrackColor(disabledTrackColor);
		}
		super.paint(graphics);
	}
}
