package com.example.storage.model.result;

public class VolumeResult {
	
	private String _id;

	private Long volume;

	public VolumeResult(String _id, Long volume) {
		super();
		this._id = _id;
		this.volume = volume;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "VolumeResult [_id=" + _id + ", volume=" + volume + "]";
	}
	
	
}
